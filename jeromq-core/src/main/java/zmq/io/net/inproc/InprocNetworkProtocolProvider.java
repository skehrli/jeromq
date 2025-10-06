package zmq.io.net.inproc;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.net.InetSocketAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import zmq.Options;
import zmq.Own;
import zmq.SocketBase;
import zmq.io.IEngine;
import zmq.io.IOThread;
import zmq.io.SessionBase;
import zmq.io.net.Address;
import zmq.io.net.Address.IZAddress;
import zmq.io.net.Listener;
import zmq.io.net.NetProtocol;
import zmq.io.net.NetworkProtocolProvider;

public class InprocNetworkProtocolProvider implements NetworkProtocolProvider<InetSocketAddress>
{
    @Pure
    @Override
    public boolean handleProtocol(NetProtocol protocol)
    {
        return protocol == NetProtocol.inproc;
    }

    @Pure
    @Override
    public Listener getListener(IOThread ioThread, SocketBase socket,
                                Options options)
    {
        return null;
    }

    @Pure
    @Override
    public IZAddress<InetSocketAddress> zresolve(String addr, boolean ipv6)
    {
        return null;
    }

    @SideEffectFree
    @Override
    public void startConnecting(Options options, IOThread ioThread,
                                SessionBase session, Address<InetSocketAddress> addr,
                                boolean delayedStart, Consumer<Own> launchChild,
                                BiConsumer<SessionBase, IEngine> sendAttach)
    {
        assert false;
    }

    @Pure
    @Override
    public boolean isValid()
    {
        return true;
    }

    @Pure
    @Override
    public boolean wantsIOThread()
    {
        return false;
    }
}
