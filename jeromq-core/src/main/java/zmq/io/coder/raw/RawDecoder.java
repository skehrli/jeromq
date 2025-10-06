package zmq.io.coder.raw;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import java.nio.ByteBuffer;

import zmq.Msg;
import zmq.io.coder.IDecoder;
import zmq.util.ValueReference;

public class RawDecoder implements IDecoder
{
    //  The buffer for data to decode.
    private final ByteBuffer buffer;

    protected Msg inProgress;

    @Impure
    public RawDecoder(int bufsize)
    {
        buffer = ByteBuffer.allocateDirect(bufsize);
        inProgress = new Msg();
    }

    @Impure
    @Override
    public ByteBuffer getBuffer()
    {
        buffer.clear();
        return buffer;
    }

    @Impure
    @Override
    public Step.Result decode(ByteBuffer buffer, int size, ValueReference<Integer> processed)
    {
        processed.set(size);
        inProgress = new Msg(size);
        inProgress.put(buffer);

        return Step.Result.DECODED;
    }

    @Pure
    @Override
    public Msg msg()
    {
        return inProgress;
    }

    @SideEffectFree
    @Override
    public void destroy()
    {
    }
}
