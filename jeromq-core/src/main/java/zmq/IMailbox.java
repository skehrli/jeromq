package zmq;

import org.checkerframework.dataflow.qual.Impure;
import java.io.Closeable;

public interface IMailbox extends Closeable
{
    @Impure
    void send(final Command cmd);

    @Impure
    Command recv(long timeout);
}
