package org.zeromq;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.io.IOException;

public class Utils
{
    @SideEffectFree
    private Utils()
    {
    }

    @Impure
    public static int findOpenPort() throws IOException
    {
        return zmq.util.Utils.findOpenPort();
    }

    @Impure
    public static void checkArgument(boolean expression, String errorMessage)
    {
        zmq.util.Utils.checkArgument(expression, errorMessage);
    }
}
