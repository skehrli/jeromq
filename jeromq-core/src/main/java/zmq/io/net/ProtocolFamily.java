package zmq.io.net;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Replacement of ProtocolFamily from SDK so it can be used in Android environments.
 */
public interface ProtocolFamily
{
    @Pure
    String name();
}
