package zmq.socket.clientserver;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZError;
import zmq.ZMQ;
import zmq.pipe.Pipe;
import zmq.socket.FQ;
import zmq.socket.LB;
import zmq.util.Blob;

public class Client extends SocketBase
{
    //  Messages are fair-queued from inbound pipes. And load-balanced to
    //  the outbound pipes.
    private final FQ fq;
    private final LB lb;

    //  Holds the prefetched message.
    @Impure
    public Client(Ctx parent, int tid, int sid)
    {
        super(parent, tid, sid, true);

        options.type = ZMQ.ZMQ_CLIENT;
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

        fq.attach(pipe);
        lb.attach(pipe);
    }

    @Impure
    @Override
    protected boolean xsend(Msg msg)
    {
        //  CLIENT sockets do not allow multipart data (ZMQ_SNDMORE)
        if (msg.hasMore()) {
            errno.set(ZError.EINVAL);
            return false;
        }
        return lb.sendpipe(msg, errno, null);
    }

    @Impure
    @Override
    protected Msg xrecv()
    {
        Msg msg = fq.recvPipe(errno, null);

        // Drop any messages with more flag
        while (msg != null && msg.hasMore()) {
            // drop all frames of the current multi-frame message
            msg = fq.recvPipe(errno, null);

            while (msg != null && msg.hasMore()) {
                fq.recvPipe(errno, null);
            }

            // get the new message
            if (msg != null) {
                fq.recvPipe(errno, null);
            }
        }

        return msg;
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
}
