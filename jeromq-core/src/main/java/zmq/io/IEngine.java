package zmq.io;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;

//  Abstract interface to be implemented by various engines.
public interface IEngine
{
    //  Plug the engine to the session.
    @Impure
    void plug(IOThread ioThread, SessionBase session);

    //  Terminate and deallocate the engine. Note that 'detached'
    //  events are not fired on termination.
    @Impure
    void terminate();

    //  This method is called by the session to signal that more
    //  messages can be written to the pipe.
    @Impure
    void restartInput();

    //  This method is called by the session to signal that there
    //  are messages to send available.
    @Impure
    void restartOutput();

    @Impure
    void zapMsgAvailable();

    @Pure
    String getEndPoint();
}
