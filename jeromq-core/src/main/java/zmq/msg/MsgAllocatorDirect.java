package zmq.msg;

import org.checkerframework.dataflow.qual.Impure;
import java.nio.ByteBuffer;

import zmq.Msg;

public class MsgAllocatorDirect implements MsgAllocator
{
    @Impure
    @Override
    public Msg allocate(int size)
    {
        return new Msg(ByteBuffer.allocateDirect(size));
    }
}
