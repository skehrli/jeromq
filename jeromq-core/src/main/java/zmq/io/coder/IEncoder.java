package zmq.io.coder;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.nio.ByteBuffer;

import zmq.Msg;
import zmq.util.ValueReference;

public interface IEncoder
{
    //  Load a new message into encoder.
    @Impure
    void loadMsg(Msg msg);

    //  The function returns a batch of binary data. The data
    //  are filled to a supplied buffer. If no buffer is supplied (data_
    //  points to NULL) decoder object will provide buffer of its own.
    @Impure
    int encode(ValueReference<ByteBuffer> data, int size);

    @SideEffectFree
    void destroy();

    // called when stream engine finished encoding all messages and is ready to
    // send data to network layer
    @Impure
    void encoded();
}
