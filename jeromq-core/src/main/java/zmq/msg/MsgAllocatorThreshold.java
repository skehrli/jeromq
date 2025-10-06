package zmq.msg;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import zmq.Config;
import zmq.Msg;

public class MsgAllocatorThreshold implements MsgAllocator
{
    private static final MsgAllocator direct = new MsgAllocatorDirect();
    private static final MsgAllocator heap   = new MsgAllocatorHeap();

    public final int threshold;

    @SideEffectFree
    @Impure
    public MsgAllocatorThreshold()
    {
        this(Config.MSG_ALLOCATION_HEAP_THRESHOLD.getValue());
    }

    @SideEffectFree
    public MsgAllocatorThreshold(int threshold)
    {
        this.threshold = threshold;
    }

    @Impure
    @Override
    public Msg allocate(int size)
    {
        if (threshold > 0 && size > threshold) {
            return direct.allocate(size);
        }
        else {
            return heap.allocate(size);
        }
    }
}
