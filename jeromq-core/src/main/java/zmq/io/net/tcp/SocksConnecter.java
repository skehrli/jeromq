package zmq.io.net.tcp;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import zmq.Options;
import zmq.io.IOThread;
import zmq.io.SessionBase;
import zmq.io.net.Address;
import zmq.io.net.NetProtocol;

// TODO continue socks connecter
public class SocksConnecter extends TcpConnecter
{
    private enum Status
    {
        UNPLUGGED,
        WAITING_FOR_RECONNECT_TIME,
        WAITING_FOR_PROXY_CONNECTION,
        SENDING_GREETING,
        WAITING_FOR_CHOICE,
        SENDING_REQUEST,
        WAITING_FOR_RESPONSE
    }

    Status status;

    // String representation of endpoint to connect to
    String endpoint;

    @Impure
    public SocksConnecter(IOThread ioThread, SessionBase session, final Options options, final Address addr,
            final Address proxyAddr, boolean delayedStart)
    {
        super(ioThread, session, options, addr, delayedStart);
        assert (NetProtocol.tcp.equals(addr.protocol()));
        endpoint = proxyAddr.toString();
        this.status = Status.UNPLUGGED;
        throw new UnsupportedOperationException("Socks connecter is not implemented");
    }

    @SideEffectFree
    @Impure
    @Override
    protected void processPlug()
    {
        if (delayedStart) {
            startTimer();
        }
        else {
            initiateConnect();
        }
    }

    @Impure
    @Override
    protected void processTerm(int linger)
    {
        switch (status) {
        case UNPLUGGED:
            break;
        case WAITING_FOR_RECONNECT_TIME:
            ioObject.cancelTimer(RECONNECT_TIMER_ID);
            break;
        case WAITING_FOR_PROXY_CONNECTION:
        case SENDING_GREETING:
        case WAITING_FOR_CHOICE:
        case SENDING_REQUEST:
        case WAITING_FOR_RESPONSE:
            close();
            break;

        default:
            break;
        }
        super.processTerm(linger);
    }

    @Impure
    @Override
    public void inEvent()
    {
        assert (status != Status.UNPLUGGED && status != Status.WAITING_FOR_RECONNECT_TIME);

        super.inEvent();
    }

    @Impure
    @Override
    public void outEvent()
    {
        super.outEvent();
    }

    @Impure
    @Override
    public void timerEvent(int id)
    {
        super.timerEvent(id);
    }

    //  Internal function to start the actual connection establishment.
    @SideEffectFree
    void initiateConnect()
    {
    }

    @Pure
    int processServerResponse()
    {
        return -1;
    }

    @SideEffectFree
    void parseAddress(String address, String hostname, int port)
    {
    }

    @SideEffectFree
    void connectToProxy()
    {
    }

    @SideEffectFree
    void error()
    {
    }

    //  Internal function to start reconnect timer
    @SideEffectFree
    void startTimer()
    {
    }

    //  Internal function to return a reconnect backoff delay.
    //  Will modify the current_reconnect_ivl used for next call
    //  Returns the currently used interval
    @Pure
    int getNewReconnectIvl()
    {
        return -1;
    }

    //  Open TCP connecting socket. Returns -1 in case of error,
    //  0 if connect was successfull immediately. Returns -1 with
    //  EAGAIN errno if async connect was launched.
    @Pure
    int open()
    {
        return -1;
    }

    //  Get the file descriptor of newly created connection. Returns
    //  retired_fd if the connection was unsuccessfull.
    @SideEffectFree
    void checkProxyConnection()
    {
    }
}
