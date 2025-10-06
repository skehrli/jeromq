package zmq.io.net.ipc;

import org.checkerframework.dataflow.qual.Impure;
import java.net.InetSocketAddress;

import zmq.Options;
import zmq.io.IOThread;
import zmq.io.SessionBase;
import zmq.io.net.Address;
import zmq.io.net.tcp.TcpConnecter;

public class IpcConnecter extends TcpConnecter
{
    @Impure
    public IpcConnecter(IOThread ioThread, SessionBase session, final Options options, final Address<InetSocketAddress> addr, boolean wait)
    {
        super(ioThread, session, options, addr, wait);
    }
}
