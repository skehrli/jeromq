package zmq.io.net.pgm;

import org.checkerframework.dataflow.qual.Pure;
import zmq.io.net.NetProtocol;

public class EpgmNetworkProtocolProvider extends PgmNetworkProtocolProvider
{
    @Pure
    @Override
    public boolean handleProtocol(NetProtocol protocol)
    {
        return protocol == NetProtocol.epgm;
    }

    @Pure
    @Override
    protected boolean withUdpEncapsulation()
    {
        return true;
    }
}
