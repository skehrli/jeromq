package zmq.poll;
import org.checkerframework.dataflow.qual.Impure;

public interface IPollEvents
{
    /**
     * Called by I/O thread when file descriptor is ready for reading.
     */
    @Impure
    default void inEvent()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Called by I/O thread when file descriptor is ready for writing.
     */
    @Impure
    default void outEvent()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Called by I/O thread when file descriptor might be ready for connecting.
     */
    @Impure
    default void connectEvent()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Called by I/O thread when file descriptor is ready for accept.
     */
    @Impure
    default void acceptEvent()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Called when timer expires.
     *
     * @param id the ID of the expired timer.
     */
    @Impure
    default void timerEvent(int id)
    {
        throw new UnsupportedOperationException();
    }
}
