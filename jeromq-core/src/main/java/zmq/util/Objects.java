package zmq.util;
import org.checkerframework.checker.mustcall.qual.MustCallAlias;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;

public class Objects
{
    @SideEffectFree
    private Objects()
    {
        // no instantiation
    }

    @Impure
    @MustCallAlias
    public static <T> T requireNonNull(@MustCallAlias T object, String msg)
    {
        Utils.checkArgument(object != null, msg);
        return object;
    }
}
