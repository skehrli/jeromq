package zmq;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import zmq.io.IEngine;
import zmq.io.IOThread;
import zmq.io.SessionBase;
import zmq.pipe.Pipe;
import zmq.pipe.YPipeBase;

//  Base class for all objects that participate in inter-thread
//  communication.
public abstract class ZObject
{
    //  Context provides access to the global state.
    private final Ctx ctx;

    //  Thread ID of the thread the object belongs to.
    private int tid;

    @SideEffectFree
    protected ZObject(Ctx ctx, int tid)
    {
        this.ctx = ctx;
        this.tid = tid;
    }

    @SideEffectFree
    @Impure
    protected ZObject(ZObject parent)
    {
        this(parent.ctx, parent.tid);
    }

    @Pure
    public final int getTid()
    {
        return tid;
    }

    @Impure
    protected final void setTid(int tid)
    {
        this.tid = tid;
    }

    @Pure
    public final Ctx getCtx()
    {
        return ctx;
    }

    @Impure
    @SuppressWarnings("unchecked")
    final void processCommand(Command cmd)
    {
        //        System.out.println(Thread.currentThread().getName() + ": Processing command " + cmd);
        switch (cmd.type) {
        case ACTIVATE_READ:
            processActivateRead();
            break;

        case ACTIVATE_WRITE:
            processActivateWrite((Long) cmd.arg);
            break;

        case STOP:
            processStop();
            break;

        case PLUG:
            processPlug();
            processSeqnum();
            break;

        case OWN:
            processOwn((Own) cmd.arg);
            processSeqnum();
            break;

        case ATTACH:
            processAttach((IEngine) cmd.arg);
            processSeqnum();
            break;

        case BIND:
            processBind((Pipe) cmd.arg);
            processSeqnum();
            break;

        case HICCUP:
            processHiccup((YPipeBase<Msg>) cmd.arg);
            break;

        case PIPE_TERM:
            processPipeTerm();
            break;

        case PIPE_TERM_ACK:
            processPipeTermAck();
            break;

        case TERM_REQ:
            processTermReq((Own) cmd.arg);
            break;

        case TERM:
            processTerm((Integer) cmd.arg);
            break;

        case TERM_ACK:
            processTermAck();
            break;

        case REAP:
            processReap((SocketBase) cmd.arg);
            break;

        case REAP_ACK:
            processReapAck();
            break;

        case REAPED:
            processReaped();
            break;

        case INPROC_CONNECTED:
            processSeqnum();
            break;

        case CANCEL:
            processCancel();
            break;

        case DONE:
        default:
            throw new IllegalArgumentException();
        }
    }

    @Impure
    protected final boolean registerEndpoint(String addr, Ctx.Endpoint endpoint)
    {
        return ctx.registerEndpoint(addr, endpoint);
    }

    @Impure
    protected final boolean unregisterEndpoint(String addr, SocketBase socket)
    {
        return ctx.unregisterEndpoint(addr, socket);
    }

    @Impure
    protected final void unregisterEndpoints(SocketBase socket)
    {
        ctx.unregisterEndpoints(socket);
    }

    @Impure
    protected final Ctx.Endpoint findEndpoint(String addr)
    {
        return ctx.findEndpoint(addr);
    }

    @Impure
    protected final void pendConnection(String addr, Ctx.Endpoint endpoint, Pipe[] pipes)
    {
        ctx.pendConnection(addr, endpoint, pipes);
    }

    @Impure
    protected final void connectPending(String addr, SocketBase bindSocket)
    {
        ctx.connectPending(addr, bindSocket);
    }

    @Impure
    protected final void destroySocket(SocketBase socket)
    {
        ctx.destroySocket(socket);
    }

    //  Chooses least loaded I/O thread.
    @Impure
    protected final IOThread chooseIoThread(long affinity)
    {
        return ctx.chooseIoThread(affinity);
    }

    @Impure
    protected final void sendStop()
    {
        //  'stop' command goes always from administrative thread to
        //  the current object.
        Command cmd = new Command(this, Command.Type.STOP);
        ctx.sendCommand(tid, cmd);
    }

    @Impure
    protected final void sendPlug(Own destination)
    {
        sendPlug(destination, true);
    }

    @Impure
    protected final void sendPlug(Own destination, boolean incSeqnum)
    {
        if (incSeqnum) {
            destination.incSeqnum();
        }

        Command cmd = new Command(destination, Command.Type.PLUG);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendOwn(Own destination, Own object)
    {
        destination.incSeqnum();
        Command cmd = new Command(destination, Command.Type.OWN, object);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendAttach(SessionBase destination, IEngine engine)
    {
        sendAttach(destination, engine, true);
    }

    @Impure
    protected final void sendAttach(SessionBase destination, IEngine engine, boolean incSeqnum)
    {
        if (incSeqnum) {
            destination.incSeqnum();
        }

        Command cmd = new Command(destination, Command.Type.ATTACH, engine);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendBind(Own destination, Pipe pipe)
    {
        sendBind(destination, pipe, true);
    }

    @Impure
    protected final void sendBind(Own destination, Pipe pipe, boolean incSeqnum)
    {
        if (incSeqnum) {
            destination.incSeqnum();
        }

        Command cmd = new Command(destination, Command.Type.BIND, pipe);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendActivateRead(Pipe destination)
    {
        Command cmd = new Command(destination, Command.Type.ACTIVATE_READ);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendActivateWrite(Pipe destination, long msgsRead)
    {
        Command cmd = new Command(destination, Command.Type.ACTIVATE_WRITE, msgsRead);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendHiccup(Pipe destination, YPipeBase<Msg> pipe)
    {
        Command cmd = new Command(destination, Command.Type.HICCUP, pipe);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendPipeTerm(Pipe destination)
    {
        Command cmd = new Command(destination, Command.Type.PIPE_TERM);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendPipeTermAck(Pipe destination)
    {
        Command cmd = new Command(destination, Command.Type.PIPE_TERM_ACK);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendTermReq(Own destination, Own object)
    {
        Command cmd = new Command(destination, Command.Type.TERM_REQ, object);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendTerm(Own destination, int linger)
    {
        Command cmd = new Command(destination, Command.Type.TERM, linger);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendTermAck(Own destination)
    {
        Command cmd = new Command(destination, Command.Type.TERM_ACK);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendReap(SocketBase socket)
    {
        Command cmd = new Command(ctx.getReaper(), Command.Type.REAP, socket);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendReapAck()
    {
        Command cmd = new Command(this, Command.Type.REAP_ACK);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendReaped()
    {
        Command cmd = new Command(ctx.getReaper(), Command.Type.REAPED);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendInprocConnected(SocketBase socket)
    {
        Command cmd = new Command(socket, Command.Type.INPROC_CONNECTED);
        sendCommand(cmd);
    }

    @Impure
    protected final void sendDone()
    {
        Command cmd = new Command(null, Command.Type.DONE);
        ctx.sendCommand(Ctx.TERM_TID, cmd);
    }

    @Impure
    protected final void sendCancel()
    {
        Command cmd = new Command(this, Command.Type.CANCEL);
        sendCommand(cmd);
    }

    @Impure
    protected void processStop()
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processPlug()
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processOwn(Own object)
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processAttach(IEngine engine)
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processBind(Pipe pipe)
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processActivateRead()
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processActivateWrite(long msgsRead)
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processHiccup(YPipeBase<Msg> hiccupPipe)
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processPipeTerm()
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processPipeTermAck()
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processTermReq(Own object)
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processTerm(int linger)
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processTermAck()
    {
        throw new UnsupportedOperationException();
    }

    @Impure
    protected void processReap(SocketBase socket)
    {
        throw new UnsupportedOperationException();
    }

    @SideEffectFree
    protected void processReapAck()
    {
    }

    @Impure
    protected void processReaped()
    {
        throw new UnsupportedOperationException();
    }

    //  Special handler called after a command that requires a seqnum
    //  was processed. The implementation should catch up with its counter
    //  of processed commands here.
    @Impure
    protected void processSeqnum()
    {
        throw new UnsupportedOperationException();
    }

    @SideEffectFree
    protected void processCancel()
    {
    }

    @Impure
    private void sendCommand(Command cmd)
    {
        ctx.sendCommand(cmd.destination.getTid(), cmd);
    }
}
