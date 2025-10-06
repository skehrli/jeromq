package org.zeromq;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import zmq.ZError;

public class ZMQException extends UncheckedZMQException
{
    private static final long serialVersionUID = -978820750094924644L;

    private final int code;

    @SideEffectFree
    @Impure
    public ZMQException(int errno)
    {
        super("Errno " + errno);
        code = errno;
    }

    @SideEffectFree
    @Impure
    public ZMQException(String message, int errno)
    {
        super(message);
        code = errno;
    }

    @SideEffectFree
    @Impure
    public ZMQException(String message, int errno, Throwable cause)
    {
        super(message, cause);
        code = errno;
    }

    @Pure
    public int getErrorCode()
    {
        return code;
    }

    @SideEffectFree
    @Impure
    @Override
    public String toString()
    {
        return super.toString() + " : " + ZError.toString(code);
    }
}
