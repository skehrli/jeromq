package zmq;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import zmq.io.net.Address;
import zmq.io.net.tcp.TcpUtils;

@Deprecated
public class Utils
{
    @SideEffectFree
    private Utils()
    {
    }

    @Impure
    public static int randomInt()
    {
        return zmq.util.Utils.randomInt();
    }

    @Impure
    public static byte[] randomBytes(int length)
    {
        return zmq.util.Utils.randomBytes(length);
    }

    @Impure
    public static int findOpenPort() throws IOException
    {
        return zmq.util.Utils.findOpenPort();
    }

    @Impure
    public static void unblockSocket(SelectableChannel... channels) throws IOException
    {
        TcpUtils.unblockSocket(channels);
    }

    @SideEffectFree
    @Impure
    public static <T> T[] realloc(Class<T> klass, T[] src, int size, boolean ended)
    {
        return zmq.util.Utils.realloc(klass, src, size, ended);
    }

    @Impure
    public static byte[] bytes(ByteBuffer buf)
    {
        return zmq.util.Utils.bytes(buf);
    }

    @SideEffectFree
    @Impure
    public static byte[] realloc(byte[] src, int size)
    {
        return zmq.util.Utils.realloc(src, size);
    }

    @Impure
    public static boolean delete(File path)
    {
        return zmq.util.Utils.delete(path);
    }

    @Impure
    public static Address getPeerIpAddress(SocketChannel fd)
    {
        return zmq.util.Utils.getPeerIpAddress(fd);
    }
}
