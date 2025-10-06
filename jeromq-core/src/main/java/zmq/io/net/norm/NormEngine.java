package zmq.io.net.norm;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import zmq.Options;
import zmq.io.EngineNotImplemented;
import zmq.io.IOThread;
import zmq.io.net.Address;

// TODO V4 implement NORM engine
public class NormEngine extends EngineNotImplemented
{
    @SideEffectFree
    @Impure
    public NormEngine(IOThread ioThread, Options options)
    {
        throw new UnsupportedOperationException();
    }

    @Pure
    public boolean init(Address addr, boolean b, boolean c)
    {
        return false;
    }
}
