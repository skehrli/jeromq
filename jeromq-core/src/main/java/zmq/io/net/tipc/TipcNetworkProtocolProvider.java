package zmq.io.net.tipc;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
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

public class TipcNetworkProtocolProvider implements NetworkProtocolProvider<InetSocketAddress>
{
    @Pure
    @Override
    public boolean handleProtocol(NetProtocol protocol)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Impure
    @Override
    public Listener getListener(IOThread ioThread, SocketBase socket,
                                Options options)
    {
        return new TipcListener(ioThread, socket, options);
    }

    @Pure
    @Override
    public IZAddress<InetSocketAddress> zresolve(String addr, boolean ipv6)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Impure
    @Override
    public void startConnecting(Options options, IOThread ioThread,
                                SessionBase session, Address<InetSocketAddress> addr,
                                boolean delayedStart, Consumer<Own> launchChild,
                                BiConsumer<SessionBase, IEngine> sendAttach)
    {
        TipcConnecter connecter = new TipcConnecter(ioThread, session, options, addr, delayedStart);
        launchChild.accept(connecter);
    }

    @Pure
    @Override
    public boolean wantsIOThread()
    {
        return true;
    }
}
