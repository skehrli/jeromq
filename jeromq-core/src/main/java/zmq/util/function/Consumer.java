package zmq.util.function;
import org.checkerframework.checker.mustcall.qual.MustCallUnknown;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Represents an operation that accepts a single input argument and returns no
 * result. Unlike most other functional interfaces, {@code Consumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a functional interface
 * whose functional method is {@link #accept(Object)}.
 *
 * @param <T> the type of the input to the operation
 *
 */
public interface Consumer<T extends @MustCallUnknown Object>
{
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    @SideEffectFree
    void accept(T t);
}
