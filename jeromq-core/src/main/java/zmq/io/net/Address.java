package zmq.io.net;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.net.SocketAddress;

public class Address<S extends SocketAddress>
{
    public interface IZAddress<SA extends SocketAddress>
    {
        @Impure
        ProtocolFamily family();

        @Impure
        String toString(int port);

        @Impure
        SA resolve(String name, boolean ipv6, boolean local);

        @Pure
        SA address();

        @Pure
        SA sourceAddress();
    }

    private final NetProtocol protocol;
    private final String      address;

    private IZAddress<S> resolved;

    /**
     * @param protocol
     * @param address
     * @throws IllegalArgumentException if the protocol name can be matched to an actual supported protocol
     */
    @Impure
    @Deprecated
    public Address(final String protocol, final String address)
    {
        this.protocol = NetProtocol.getProtocol(protocol);
        this.address = address;
        resolved = null;
    }

    /**
     * @param protocol
     * @param address
     */
    @SideEffectFree
    public Address(final NetProtocol protocol, final String address)
    {
        this.protocol = protocol;
        this.address = address;
        resolved = null;
    }

    /**
     * @param socketAddress
     * @throws IllegalArgumentException if the SocketChannel is not an IP socket address
     */
    @Impure
    public Address(SocketAddress socketAddress)
    {
        protocol = NetProtocol.findByAddress(socketAddress);
        address = protocol.formatSocketAddress(socketAddress);
        resolved = null;
     }

    @SideEffectFree
    @Impure
    @Override
    public String toString()
    {
        if (isResolved()) {
            return resolved.toString();
        }
        else if (protocol != null && !address.isEmpty()) {
            return protocol.name() + "://" + address;
        }
        else {
            return "";
        }
    }

    @Pure
    public NetProtocol protocol()
    {
        return protocol;
    }

    @Pure
    public String address()
    {
        return address;
    }

    @SideEffectFree
    public String host()
    {
        final int portDelimiter = address.lastIndexOf(':');
        if (portDelimiter > 0) {
            return address.substring(0, portDelimiter);
        }
        return address;
    }

    @Pure
    public IZAddress<S> resolved()
    {
        return resolved;
    }

    @Pure
    public boolean isResolved()
    {
        return resolved != null;
    }

    @Impure
    public IZAddress<S> resolve(boolean ipv6)
    {
        resolved = protocol.zresolve(address, ipv6);
        return resolved;
    }
}
