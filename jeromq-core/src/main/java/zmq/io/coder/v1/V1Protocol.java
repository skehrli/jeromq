package zmq.io.coder.v1;
import org.checkerframework.dataflow.qual.Pure;

public interface V1Protocol
{
    int VERSION = 1;

    int MORE_FLAG = 1;

    // make checkstyle not block the release
    @Pure
    @Override
    String toString();
}
