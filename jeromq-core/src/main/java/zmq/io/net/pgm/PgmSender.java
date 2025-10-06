package zmq.io.net.pgm;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import zmq.Options;
import zmq.io.EngineNotImplemented;
import zmq.io.IOThread;
import zmq.io.net.Address;

// TODO V4 implement pgm sender
public class PgmSender extends EngineNotImplemented
{
    @SideEffectFree
    @Impure
    public PgmSender(IOThread ioThread, Options options)
    {
        throw new UnsupportedOperationException();
    }

    @Pure
    public boolean init(boolean udpEncapsulation, Address addr)
    {
        return false;
    }
}
