package zmq.socket;

import org.checkerframework.dataflow.qual.Impure;
import zmq.Ctx;
import zmq.ZMQ;

public class Raw extends Peer
{
    @Impure
    public Raw(Ctx parent, int tid, int sid)
    {
        super(parent, tid, sid);

        options.type = ZMQ.ZMQ_RAW;
        options.canSendHelloMsg = true;
        options.rawSocket = true;
    }
}
