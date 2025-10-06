package zmq.socket.pubsub;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import zmq.Ctx;
import zmq.Msg;
import zmq.ZError;
import zmq.ZMQ;
import zmq.pipe.Pipe;

public class Pub extends XPub
{
    @Impure
    public Pub(Ctx parent, int tid, int sid)
    {
        super(parent, tid, sid);
        options.type = ZMQ.ZMQ_PUB;
    }

    @Impure
    @Override
    protected void xattachPipe(Pipe pipe, boolean subscribeToAll, boolean isLocallyInitiated)
    {
        assert (pipe != null);

        //  Don't delay pipe termination as there is no one
        //  to receive the delimiter.
        pipe.setNoDelay();

        super.xattachPipe(pipe, subscribeToAll, isLocallyInitiated);
    }

    @Impure
    @Override
    protected Msg xrecv()
    {
        errno.set(ZError.ENOTSUP);
        //  Messages cannot be received from PUB socket.
        throw new UnsupportedOperationException();
    }

    @Pure
    @Override
    protected boolean xhasIn()
    {
        return false;
    }
}
