package zmq.io.mechanism.gssapi;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import zmq.Msg;
import zmq.Options;
import zmq.io.SessionBase;
import zmq.io.mechanism.Mechanism;
import zmq.io.net.Address;

// TODO V4 implement GSSAPI
public class GssapiServerMechanism extends Mechanism
{
    @SideEffectFree
    @Impure
    public GssapiServerMechanism(SessionBase session, Address peerAddress, Options options)
    {
        super(session, peerAddress, options);
        throw new UnsupportedOperationException("GSSAPI mechanism is not yet implemented");
    }

    @Pure
    @Override
    public Status status()
    {
        return null;
    }

    @Pure
    @Override
    public int zapMsgAvailable()
    {
        return 0;
    }

    @Pure
    @Override
    public int processHandshakeCommand(Msg msg)
    {
        return 0;
    }

    @Pure
    @Override
    public int nextHandshakeCommand(Msg msg)
    {
        return 0;
    }
}
