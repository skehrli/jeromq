package zmq.util;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.mustcall.qual.NotOwning;

public class ValueReference<V>
{
    private V value;

    @SideEffectFree
    public ValueReference(V value)
    {
        this.value = value;
    }

    @SideEffectFree
    public ValueReference()
    {
    }

    @NotOwning
    @Pure
    public final V get()
    {
        return value;
    }

    @Impure
    public final void set(V value)
    {
        this.value = value;
    }

    @SideEffectFree
    @Override
    public String toString()
    {
        return value == null ? "null" : value.toString();
    }
}
