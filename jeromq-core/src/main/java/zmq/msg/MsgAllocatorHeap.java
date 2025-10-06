package zmq.msg;

import org.checkerframework.dataflow.qual.Impure;
import zmq.Msg;

public class MsgAllocatorHeap implements MsgAllocator
{
    @Impure
    @Override
    public Msg allocate(int size)
    {
        return new Msg(size);
    }
}
