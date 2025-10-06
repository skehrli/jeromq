package zmq.io.net;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import java.net.SocketAddress;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import zmq.Options;
import zmq.Own;
import zmq.SocketBase;
import zmq.io.IEngine;
import zmq.io.IOThread;
import zmq.io.SessionBase;
import zmq.io.net.Address.IZAddress;

public interface NetworkProtocolProvider<SA extends SocketAddress>
{
    @Pure
    boolean handleProtocol(NetProtocol protocol);
    @Impure
    Listener getListener(IOThread ioThread, SocketBase socket, Options options);
    @Impure
    IZAddress<SA> zresolve(String addr, boolean ipv6);
    @Impure
    void startConnecting(Options options, IOThread ioThread, SessionBase session, Address<SA> addr, boolean delayedStart,
            Consumer<Own> launchChild, BiConsumer<SessionBase, IEngine> sendAttach);
    @Pure
    default boolean isValid()
    {
        return false;
    }
    @Pure
    default boolean handleAdress(SocketAddress socketAddress)
    {
        return false;
    }
    @Impure
    default String formatSocketAddress(SA socketAddress)
    {
        throw new IllegalArgumentException("Unhandled address protocol " + socketAddress);
    }
    @Pure
    boolean wantsIOThread();
}
