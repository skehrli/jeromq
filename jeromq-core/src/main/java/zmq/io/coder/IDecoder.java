package zmq.io.coder;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import java.nio.ByteBuffer;

import zmq.Msg;
import zmq.util.ValueReference;

public interface IDecoder
{
    interface Step
    {
        enum Result
        {
            MORE_DATA,
            DECODED,
            ERROR;
        }

        @Impure
        Result apply();
    }

    @Impure
    ByteBuffer getBuffer();

    @Impure
    Step.Result decode(ByteBuffer buffer, int size, ValueReference<Integer> processed);

    @Pure
    Msg msg();

    @SideEffectFree
    void destroy();
}
