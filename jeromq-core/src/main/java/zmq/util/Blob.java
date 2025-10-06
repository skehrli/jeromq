package zmq.util;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.util.Arrays;

import zmq.Msg;

public class Blob
{
    private final byte[] buf;

    @SideEffectFree
    private Blob(byte[] data)
    {
        buf = data;
    }

    @SideEffectFree
    @Impure
    private static Blob createBlob(byte[] data, boolean copy)
    {
        if (copy) {
            byte[] b = new byte[data.length];
            System.arraycopy(data, 0, b, 0, data.length);
            return new Blob(b);
        }
        else {
            return new Blob(data);
        }
    }

    @Impure
    public static Blob createBlob(Msg msg)
    {
        return createBlob(msg.data(), true);
    }

    @SideEffectFree
    @Impure
    public static Blob createBlob(byte[] data)
    {
        return createBlob(data, false);
    }

    @Pure
    public int size()
    {
        return buf.length;
    }

    @Pure
    public byte[] data()
    {
        return buf;
    }

    @Pure
    @Override
    public boolean equals(Object t)
    {
        if (t instanceof Blob) {
            return Arrays.equals(buf, ((Blob) t).buf);
        }
        return false;
    }

    @Pure
    @Override
    public int hashCode()
    {
        return Arrays.hashCode(buf);
    }
}
