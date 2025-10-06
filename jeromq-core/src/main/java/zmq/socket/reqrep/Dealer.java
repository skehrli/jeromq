package zmq.socket.reqrep;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import zmq.Ctx;
import zmq.Msg;
import zmq.Options;
import zmq.SocketBase;
import zmq.ZError;
import zmq.ZMQ;
import zmq.pipe.Pipe;
import zmq.socket.FQ;
import zmq.socket.LB;
import zmq.util.Blob;
import zmq.util.ValueReference;

public class Dealer extends SocketBase
{
    //  Messages are fair-queued from inbound pipes. And load-balanced to
    //  the outbound pipes.
    private final FQ fq;
    private final LB lb;

    // if true, send an empty message to every connected router peer
    private boolean probeRouter;

    //  Holds the prefetched message.
    @Impure
    public Dealer(Ctx parent, int tid, int sid)
    {
        super(parent, tid, sid);

        options.type = ZMQ.ZMQ_DEALER;
        options.canSendHelloMsg = true;
        options.canReceiveHiccupMsg = true;

        fq = new FQ();
        lb = new LB();
    }

    @Impure
    @Override
    protected void xattachPipe(Pipe pipe, boolean subscribe2all, boolean isLocallyInitiated)
    {
        assert (pipe != null);

        if (probeRouter) {
            Msg probe = new Msg();
            pipe.write(probe);
            // assert (rc == 0) is not applicable here, since it is not a bug.
            pipe.flush();
        }
        fq.attach(pipe);
        lb.attach(pipe);
    }

    @Impure
    @Override
    protected boolean xsetsockopt(int option, Object optval)
    {
        if (option == ZMQ.ZMQ_PROBE_ROUTER) {
            probeRouter = Options.parseBoolean(option, optval);
            return true;
        }
        errno.set(ZError.EINVAL);
        return false;
    }

    @Impure
    @Override
    protected boolean xsend(Msg msg)
    {
        return sendpipe(msg, null);
    }

    @Impure
    @Override
    protected Msg xrecv()
    {
        return recvpipe(null);
    }

    @Impure
    @Override
    protected boolean xhasIn()
    {
        return fq.hasIn();
    }

    @Impure
    @Override
    protected boolean xhasOut()
    {
        return lb.hasOut();
    }

    @Pure
    @Impure
    @Override
    protected Blob getCredential()
    {
        return fq.getCredential();
    }

    @Impure
    @Override
    protected void xreadActivated(Pipe pipe)
    {
        fq.activated(pipe);
    }

    @Impure
    @Override
    protected void xwriteActivated(Pipe pipe)
    {
        lb.activated(pipe);
    }

    @Impure
    @Override
    protected void xpipeTerminated(Pipe pipe)
    {
        fq.terminated(pipe);
        lb.terminated(pipe);
    }

    @Impure
    protected final boolean sendpipe(Msg msg, ValueReference<Pipe> pipe)
    {
        return lb.sendpipe(msg, errno, pipe);
    }

    @Impure
    protected final Msg recvpipe(ValueReference<Pipe> pipe)
    {
        return fq.recvPipe(errno, pipe);
    }
}
