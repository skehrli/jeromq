package zmq.msg;

import org.checkerframework.dataflow.qual.Impure;
import zmq.Msg;

public interface MsgAllocator
{
    @Impure
    Msg allocate(int size);
}
