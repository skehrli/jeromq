package zmq.io.net;

import org.checkerframework.dataflow.qual.Impure;
import zmq.Options;
import zmq.Own;
import zmq.SocketBase;
import zmq.io.IOThread;
import zmq.poll.IPollEvents;

public abstract class Listener extends Own implements IPollEvents
{
    //  Socket the listener belongs to.
    protected final SocketBase socket;

    @Impure
    protected Listener(IOThread ioThread, SocketBase socket, final Options options)
    {
        super(ioThread, options);
        this.socket = socket;
    }

    @Impure
    public abstract boolean setAddress(String addr);

    @Impure
    public abstract String getAddress();
}
