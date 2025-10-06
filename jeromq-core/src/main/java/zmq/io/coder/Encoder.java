package zmq.io.coder;

import org.checkerframework.dataflow.qual.Impure;
import zmq.util.Errno;

public abstract class Encoder extends EncoderBase
{
    protected final Runnable sizeReady = this::sizeReady;

    protected final Runnable messageReady = this::messageReady;

    @Impure
    protected Encoder(Errno errno, int bufsize)
    {
        super(errno, bufsize);
    }

    @Impure
    protected abstract void sizeReady();

    @Impure
    protected abstract void messageReady();
}
