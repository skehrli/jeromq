package zmq.socket;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.calledmethods.qual.EnsuresCalledMethods;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZError;
import zmq.ZMQ;
import zmq.pipe.Pipe;
import zmq.util.Blob;

public class Pair extends SocketBase
{
    private Pipe pipe;
    private Pipe lastIn;

    private Blob savedCredential;

    @Impure
    public Pair(Ctx parent, int tid, int sid)
    {
        super(parent, tid, sid);
        options.type = ZMQ.ZMQ_PAIR;
    }

    @EnsuresCalledMethods(value="this.super", methods="destroy")
    @Impure
    @Override
    protected void destroy()
    {
        super.destroy();
        assert (pipe == null);
    }

    @Impure
    @Override
    protected void xattachPipe(Pipe pipe, boolean subscribe2all, boolean isLocallyInitiated)
    {
        assert (pipe != null);

        //  ZMQ_PAIR socket can only be connected to a single peer.
        //  The socket rejects any further connection requests.
        if (this.pipe == null) {
            this.pipe = pipe;
        }
        else {
            pipe.terminate(false);
        }
    }

    @Impure
    @Override
    protected void xpipeTerminated(Pipe pipe)
    {
        if (this.pipe == pipe) {
            if (lastIn == pipe) {
                savedCredential = lastIn.getCredential();
                lastIn = null;
            }
            this.pipe = null;
        }
    }

    @SideEffectFree
    @Override
    protected void xreadActivated(Pipe pipe)
    {
        //  There's just one pipe. No lists of active and inactive pipes.
        //  There's nothing to do here.
    }

    @SideEffectFree
    @Override
    protected void xwriteActivated(Pipe pipe)
    {
        //  There's just one pipe. No lists of active and inactive pipes.
        //  There's nothing to do here.
    }

    @Impure
    @Override
    protected boolean xsend(Msg msg)
    {
        if (pipe == null || !pipe.write(msg)) {
            errno.set(ZError.EAGAIN);
            return false;
        }

        if (!msg.hasMore()) {
            pipe.flush();
        }

        return true;
    }

    @Impure
    @Override
    protected Msg xrecv()
    {
        //  Deallocate old content of the message.
        Msg msg;
        if (pipe == null) {
            //  Initialize the output parameter to be a 0-byte message.
            errno.set(ZError.EAGAIN);
            return null;
        }
        else {
            msg = pipe.read();
            if (msg == null) {
                //  Initialize the output parameter to be a 0-byte message.
                errno.set(ZError.EAGAIN);
                return null;
            }
        }
        lastIn = pipe;
        return msg;
    }

    @Impure
    @Override
    protected boolean xhasIn()
    {
        return pipe != null && pipe.checkRead();
    }

    @Impure
    @Override
    protected boolean xhasOut()
    {
        return pipe != null && pipe.checkWrite();
    }

    @Pure
    @Impure
    @Override
    protected Blob getCredential()
    {
        return lastIn != null ? lastIn.getCredential() : savedCredential;
    }
}
