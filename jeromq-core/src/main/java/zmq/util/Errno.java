package zmq.util;
import org.checkerframework.dataflow.qual.Impure;

// Emulates the errno mechanism present in C++, in a per-thread basis.
public final class Errno
{
    private static final ThreadLocal<Integer> local = ThreadLocal.withInitial(() -> 0);

    @Impure
    public int get()
    {
        return local.get();
    }

    @Impure
    public void set(int errno)
    {
        local.set(errno);
    }

    @Impure
    public boolean is(int err)
    {
        return get() == err;
    }

    @Impure
    @Override
    public String toString()
    {
        return "Errno[" + get() + "]";
    }
}
