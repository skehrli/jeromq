package zmq.io;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.util.HashSet;
import java.util.Set;

import zmq.Ctx;
import zmq.Msg;
import zmq.Options;
import zmq.Own;
import zmq.SocketBase;
import zmq.ZError;
import zmq.ZMQ;
import zmq.ZObject;
import zmq.io.StreamEngine.ErrorReason;
import zmq.io.mechanism.Mechanisms;
import zmq.io.net.Address;
import zmq.io.net.NetProtocol;
import zmq.pipe.Pipe;
import zmq.poll.IPollEvents;

public class SessionBase extends Own implements Pipe.IPipeEvents, IPollEvents
{
    //  If true, this session (re)connects to the peer. Otherwise, it's
    //  a transient session created by the listener.
    private final boolean active;

    //  Pipe connecting the session to its socket.
    private Pipe pipe;

    //  Pipe used to exchange messages with ZAP socket.
    private Pipe zapPipe;

    //  This set is added to with pipes we are disconnecting, but haven't yet completed
    private final Set<Pipe> terminatingPipes;

    //  This flag is true if the remainder of the message being processed
    //  is still in the in pipe.
    private boolean incompleteIn;

    //  True if termination have been suspended to push the pending
    //  messages to the network.
    private boolean pending;

    //  The protocol I/O engine connected to the session.
    private IEngine engine;

    //  The socket the session belongs to.
    protected final SocketBase socket;

    //  I/O thread the session is living in. It will be used to plug in
    //  the engines into the same thread.
    private final IOThread ioThread;

    //  ID of the linger timer
    private static final int LINGER_TIMER_ID = 0x20;

    //  True is linger timer is running.
    private boolean hasLingerTimer;

    //  Protocol and address to use when connecting.
    private final Address addr;

    private final IOObject ioObject;

    @Impure
    public SessionBase(IOThread ioThread, boolean connect, SocketBase socket, Options options, Address addr)
    {
        super(ioThread, options);
        ioObject = new IOObject(ioThread, this);

        this.active = connect;
        pipe = null;
        zapPipe = null;
        incompleteIn = false;
        pending = false;
        engine = null;
        this.socket = socket;
        this.ioThread = ioThread;
        hasLingerTimer = false;
        this.addr = addr;

        terminatingPipes = new HashSet<>();
    }

    @Impure
    @Override
    public void destroy()
    {
        assert (pipe == null);
        assert (zapPipe == null);

        //  If there's still a pending linger timer, remove it.
        if (hasLingerTimer) {
            ioObject.cancelTimer(LINGER_TIMER_ID);
            hasLingerTimer = false;
        }

        //  Close the engine.
        if (engine != null) {
            engine.terminate();
        }
        ioObject.unplug();
    }

    //  To be used once only, when creating the session.
    @Impure
    public void attachPipe(Pipe pipe)
    {
        assert (!isTerminating());
        assert (this.pipe == null);
        assert (pipe != null);
        this.pipe = pipe;
        this.pipe.setEventSink(this);
    }

    @Impure
    protected Msg pullMsg()
    {
        if (pipe == null) {
            return null;
        }

        Msg msg = pipe.read();
        if (msg == null) {
            return null;
        }
        incompleteIn = msg.hasMore();

        return msg;

    }

    @Impure
    protected boolean pushMsg(Msg msg)
    {
        if (msg.isCommand()) {
            return true;
        }
        if (pipe != null && pipe.write(msg)) {
            return true;
        }
        errno.set(ZError.EAGAIN);
        return false;
    }

    @Impure
    public Msg readZapMsg()
    {
        if (zapPipe == null) {
            errno.set(ZError.ENOTCONN);
            return null;
        }
        Msg msg = zapPipe.read();
        if (msg == null) {
            errno.set(ZError.EAGAIN);
        }
        return msg;
    }

    @Impure
    public boolean writeZapMsg(Msg msg)
    {
        if (zapPipe == null) {
            errno.set(ZError.ENOTCONN);
            return false;
        }
        boolean rc = zapPipe.write(msg);
        assert (rc);
        if (!msg.hasMore()) {
            zapPipe.flush();
        }
        return true;
    }

    @Impure
    protected void reset()
    {
    }

    @Impure
    public void flush()
    {
        if (pipe != null) {
            pipe.flush();
        }
    }

    //  Remove any half processed messages. Flush unflushed messages.
    //  Call this function when engine disconnect to get rid of leftovers.
    @Impure
    private void cleanPipes()
    {
        assert (pipe != null);

        //  Get rid of half-processed messages in the out pipe. Flush any
        //  unflushed messages upstream.
        pipe.rollback();
        pipe.flush();
        //  Remove any half-read message from the in pipe.
        while (incompleteIn) {
            Msg msg = pullMsg();
            if (msg == null) {
                assert (!incompleteIn);
                break;
            }
            // msg.close ();
        }
    }

    @Impure
    @Override
    public void pipeTerminated(Pipe pipe)
    {
        //  Drop the reference to the deallocated pipe.
        assert (this.pipe == pipe || this.zapPipe == pipe || terminatingPipes.contains(pipe));

        if (this.pipe == pipe) {
            // If this is our current pipe, remove it
            this.pipe = null;
            if (hasLingerTimer) {
                ioObject.cancelTimer(LINGER_TIMER_ID);
                hasLingerTimer = false;
            }
        }
        else if (zapPipe == pipe) {
            zapPipe = null;
        }
        else {
            // Remove the pipe from the detached pipes set
            terminatingPipes.remove(pipe);
        }

        if (!isTerminating() && options.rawSocket) {
            if (engine != null) {
                engine.terminate();
                engine = null;
            }
        }

        //  If we are waiting for pending messages to be sent, at this point
        //  we are sure that there will be no more messages and we can proceed
        //  with termination safely.
        if (pending && this.pipe == null && this.zapPipe == null && terminatingPipes.isEmpty()) {
            pending = false;
            super.processTerm(0);
        }
    }

    @Impure
    @Override
    public void readActivated(Pipe pipe)
    {
        // Skip activating if we're detaching this pipe
        if (this.pipe != pipe && this.zapPipe != pipe) {
            assert (terminatingPipes.contains(pipe));
            return;
        }

        if (engine == null) {
            this.pipe.checkRead();
            return;
        }
        if (this.pipe == pipe) {
            engine.restartOutput();
        }
        else {
            engine.zapMsgAvailable();
        }
    }

    @Impure
    @Override
    public void writeActivated(Pipe pipe)
    {
        // Skip activating if we're detaching this pipe
        if (this.pipe != pipe) {
            assert (terminatingPipes.contains(pipe));
            return;
        }

        if (engine != null) {
            engine.restartInput();
        }
    }

    @SideEffectFree
    @Override
    public void hiccuped(Pipe pipe)
    {
        //  Hiccups are always sent from session to socket, not the other
        //  way round.
        throw new UnsupportedOperationException("Must Override");

    }

    @Pure
    public SocketBase getSocket()
    {
        return socket;
    }

    @Impure
    @Override
    protected void processPlug()
    {
        ioObject.plug();
        if (active) {
            startConnecting(false);
        }
    }

    @Impure
    public int zapConnect()
    {
        // Session might be reused with zap connexion already established, don't panic
        if (zapPipe == null) {
            Ctx.Endpoint peer = findEndpoint("inproc://zeromq.zap.01");
            if (peer.socket == null) {
                errno.set(ZError.ECONNREFUSED);
                return ZError.ECONNREFUSED;
            }
            if (peer.options.type != ZMQ.ZMQ_REP && peer.options.type != ZMQ.ZMQ_ROUTER &&
                        peer.options.type != ZMQ.ZMQ_SERVER) {
                errno.set(ZError.ECONNREFUSED);
                return ZError.ECONNREFUSED;
            }

            //  Create a bi-directional pipe that will connect
            //  session with zap socket.
            ZObject[] parents = { this, peer.socket };
            int[] hwms = { 0, 0 };
            boolean[] conflates = { false, false };
            Pipe[] pipes = Pipe.pair(parents, hwms, conflates);

            //  Attach local end of the pipe to this socket object.
            zapPipe = pipes[0];
            zapPipe.setNoDelay();
            zapPipe.setEventSink(this);

            sendBind(peer.socket, pipes[1], false);

            //  Send empty identity if required by the peer.
            if (peer.options.recvIdentity) {
                Msg id = new Msg();
                id.setFlags(Msg.IDENTITY);
                zapPipe.write(id);
                zapPipe.flush();
            }
        }
        return 0;
    }

    @Pure
    protected boolean zapEnabled()
    {
        return options.mechanism != Mechanisms.NULL || (options.zapDomain != null && !options.zapDomain.isEmpty());
    }

    @Impure
    @Override
    protected void processAttach(IEngine engine)
    {
        assert (engine != null);

        //  Create the pipe if it does not exist yet.
        if (pipe == null && !isTerminating()) {
            ZObject[] parents = { this, socket };
            boolean conflate = options.conflate && (options.type == ZMQ.ZMQ_DEALER || options.type == ZMQ.ZMQ_PULL
                    || options.type == ZMQ.ZMQ_PUSH || options.type == ZMQ.ZMQ_PUB || options.type == ZMQ.ZMQ_SUB);

            int[] hwms = { conflate ? -1 : options.recvHwm, conflate ? -1 : options.sendHwm };
            boolean[] conflates = { conflate, conflate };
            Pipe[] pipes = Pipe.pair(parents, hwms, conflates);

            //  Plug the local end of the pipe.
            pipes[0].setEventSink(this);

            //  Remember the local end of the pipe.
            assert (pipe == null);
            pipe = pipes[0];

            //  Ask socket to plug into the remote end of the pipe.
            sendBind(socket, pipes[1]);
        }

        //  Plug in the engine.
        assert (this.engine == null);
        this.engine = engine;
        this.engine.plug(ioThread, this);
    }

    @Impure
    public void engineError(boolean handshaked, ErrorReason reason)
    {
        //  Engine is dead. Let's forget about it.
        engine = null;

        //  Remove any half-done messages from the pipes.
        if (pipe != null) {
            cleanPipes();

            //  Only send disconnect message if socket was accepted and handshake was completed
            if (!active && handshaked && options.canReceiveDisconnectMsg && options.disconnectMsg != null) {
                pipe.setDisconnectMsg(options.disconnectMsg);
                pipe.sendDisconnectMsg();
            }

            if (active && handshaked && options.canReceiveHiccupMsg && options.hiccupMsg != null) {
                pipe.sendHiccupMsg(options.hiccupMsg);
            }
        }

        assert (reason == ErrorReason.CONNECTION || reason == ErrorReason.TIMEOUT || reason == ErrorReason.PROTOCOL);

        switch (reason) {
        case TIMEOUT:
        case CONNECTION:
            if (active) {
                reconnect();
            }
            else {
                terminate();
            }
            break;
        case PROTOCOL:
            terminate();
            break;
        default:
            break;
        }

        //  Just in case there's only a delimiter in the pipe.
        if (pipe != null) {
            pipe.checkRead();
        }
        if (zapPipe != null) {
            zapPipe.checkRead();
        }
    }

    @Impure
    @Override
    protected void processTerm(int linger)
    {
        assert (!pending);

        //  If the termination of the pipe happens before the term command is
        //  delivered there's nothing much to do. We can proceed with the
        //  standard termination immediately.
        if (pipe == null && zapPipe == null && terminatingPipes.isEmpty()) {
            super.processTerm(0);
            return;
        }

        pending = true;

        if (pipe != null) {
            //  If there's finite linger value, delay the termination.
            //  If linger is infinite (negative) we don't even have to set
            //  the timer.
            if (linger > 0) {
                assert (!hasLingerTimer);
                ioObject.addTimer(linger, LINGER_TIMER_ID);
                hasLingerTimer = true;
            }

            //  Start pipe termination process. Delay the termination till all messages
            //  are processed in case the linger time is non-zero.
            pipe.terminate(linger != 0);

            //  TODO: Should this go into pipe_t::terminate ?
            //  In case there's no engine and there's only delimiter in the
            //  pipe it wouldn't be ever read. Thus we check for it explicitly.
            if (engine == null) {
                pipe.checkRead();
            }
        }

        if (zapPipe != null) {
            zapPipe.terminate(false);
        }
    }

    @Impure
    @Override
    public void timerEvent(int id)
    {
        //  Linger period expired. We can proceed with termination even though
        //  there are still pending messages to be sent.
        assert (id == LINGER_TIMER_ID);
        hasLingerTimer = false;

        //  Ask pipe to terminate even though there may be pending messages in it.
        assert (pipe != null);
        pipe.terminate(false);
    }

    @Impure
    private void reconnect()
    {
        // TODO V4 -        //  Transient session self-destructs after peer disconnects. ?

        //  For delayed connect situations, terminate the pipe
        //  and reestablish later on
        if (pipe != null && !options.immediate && !addr.protocol().isMulticast) {
            pipe.hiccup();
            pipe.terminate(false);
            terminatingPipes.add(pipe);
            pipe = null;
        }

        reset();

        //  Reconnect.
        if (options.reconnectIvl != -1) {
            startConnecting(true);
        }

        //  For subscriber sockets we hiccup the inbound pipe, which will cause
        //  the socket object to resend all the subscriptions.
        if (pipe != null && (options.type == ZMQ.ZMQ_SUB || options.type == ZMQ.ZMQ_XSUB)) {
            pipe.hiccup();
        }
    }

    @Impure
    private void startConnecting(boolean wait)
    {
        assert (active);

        //  Choose I/O thread to run connecter in. Given that we are already
        //  running in an I/O thread, there must be at least one available.
        IOThread ioThread = chooseIoThread(options.affinity);
        assert (ioThread != null);

        //  Create the connecter object.

        NetProtocol protocol = addr.protocol();
        if (protocol == null) {
            errno.set(ZError.EPROTONOSUPPORT);
            return;
        }
        protocol.startConnecting(options, ioThread, this, addr, wait, this::launchChild, null);
    }

    @Pure
    @Impure
    public String getEndpoint()
    {
        return engine.getEndPoint();
    }

    @Impure
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "-" + socket;
    }

    @Impure
    @Override
    public final void incSeqnum()
    {
        super.incSeqnum();
    }
}
