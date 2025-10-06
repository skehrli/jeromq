package zmq.socket.pipeline;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZMQ;
import zmq.pipe.Pipe;
import zmq.socket.FQ;
import zmq.util.Blob;

public class Pull extends SocketBase
{
    //  Fair queueing object for inbound pipes.
    private final FQ fq;

    @Impure
    public Pull(Ctx parent, int tid, int sid)
    {
        super(parent, tid, sid);
        options.type = ZMQ.ZMQ_PULL;

        fq = new FQ();
    }

    @Impure
    @Override
    protected void xattachPipe(Pipe pipe, boolean subscribe2all, boolean isLocallyInitiated)
    {
        assert (pipe != null);
        fq.attach(pipe);
    }

    @Impure
    @Override
    protected void xreadActivated(Pipe pipe)
    {
        fq.activated(pipe);
    }

    @Impure
    @Override
    protected void xpipeTerminated(Pipe pipe)
    {
        fq.terminated(pipe);
    }

    @Impure
    @Override
    public Msg xrecv()
    {
        return fq.recv(errno);
    }

    @Impure
    @Override
    protected boolean xhasIn()
    {
        return fq.hasIn();
    }

    @Pure
    @Impure
    @Override
    protected Blob getCredential()
    {
        return fq.getCredential();
    }
}
