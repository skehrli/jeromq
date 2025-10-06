package zmq.util;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.util.concurrent.TimeUnit;

public class Clock
{
    //  TSC timestamp of when last time measurement was made.
    // private long last_tsc;

    //  Physical time corresponding to the TSC above (in milliseconds).
    // private long last_time;

    @SideEffectFree
    private Clock()
    {
    }

    /**
     * High precision timestamp in microseconds.
     */
    @Impure
    public static long nowUS()
    {
        return TimeUnit.MICROSECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    /**
     * High precision timestamp in nanoseconds.
     */
    @Impure
    public static long nowNS()
    {
        return System.nanoTime();
    }

    //  Low precision timestamp. In tight loops generating it can be
    //  10 to 100 times faster than the high precision timestamp.
    @Impure
    public static long nowMS()
    {
        return System.currentTimeMillis();
    }

    //  CPU's timestamp counter. Returns 0 if it's not available.
    @Pure
    public static long rdtsc()
    {
        return 0;
    }
}
