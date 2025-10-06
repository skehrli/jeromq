package zmq.socket.pipeline;

import org.checkerframework.dataflow.qual.Impure;
import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZMQ;
import zmq.pipe.Pipe;
import zmq.socket.LB;

public class Push extends SocketBase
{
    //  Load balancer managing the outbound pipes.
    private final LB lb;

    @Impure
    public Push(Ctx parent, int tid, int sid)
    {
        super(parent, tid, sid);
        options.type = ZMQ.ZMQ_PUSH;

        lb = new LB();
    }

    @Impure
    @Override
    protected void xattachPipe(Pipe pipe, boolean subscribe2all, boolean isLocallyInitiated)
    {
        assert (pipe != null);

        //  Don't delay pipe termination as there is no one
        //  to receive the delimiter.
        pipe.setNoDelay();

        lb.attach(pipe);
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
        lb.terminated(pipe);
    }

    @Impure
    @Override
    public boolean xsend(Msg msg)
    {
        return lb.sendpipe(msg, errno, null);
    }

    @Impure
    @Override
    protected boolean xhasOut()
    {
        return lb.hasOut();
    }
}
