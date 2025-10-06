package org.zeromq;
import org.checkerframework.dataflow.qual.SideEffectFree;

public abstract class UncheckedZMQException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    @SideEffectFree
    public UncheckedZMQException()
    {
        super();
    }

    @SideEffectFree
    public UncheckedZMQException(String message)
    {
        super(message);
    }

    @SideEffectFree
    public UncheckedZMQException(Throwable cause)
    {
        super(cause);
    }

    @SideEffectFree
    public UncheckedZMQException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
