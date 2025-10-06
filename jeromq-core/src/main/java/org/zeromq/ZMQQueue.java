package org.zeromq;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class ZMQQueue implements Runnable
{
    private final Socket inSocket;
    private final Socket outSocket;

    /**
     * Class constructor.
     *
     * @param context
     *            a 0MQ context previously created.
     * @param inSocket
     *            input socket
     * @param outSocket
     *            output socket
     */
    @SideEffectFree
    public ZMQQueue(Context context, Socket inSocket, Socket outSocket)
    {
        this.inSocket = inSocket;
        this.outSocket = outSocket;
    }

    @Impure
    @Override
    public void run()
    {
        zmq.ZMQ.proxy(inSocket.base(), outSocket.base(), null);
    }
}
