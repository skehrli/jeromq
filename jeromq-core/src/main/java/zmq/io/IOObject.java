package zmq.io;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import java.nio.channels.SelectableChannel;

import zmq.poll.IPollEvents;
import zmq.poll.Poller;
import zmq.poll.Poller.Handle;

//  Simple base class for objects that live in I/O threads.
//  It makes communication with the poller object easier and
//  makes defining unneeded event handlers unnecessary.
public class IOObject implements IPollEvents
{
    private final Poller      poller;
    private final IPollEvents handler;

    private boolean alive;

    @SideEffectFree
    @Impure
    public IOObject(IOThread ioThread, IPollEvents handler)
    {
        assert (ioThread != null);
        assert (handler != null);

        this.handler = handler;
        //  Retrieve the poller from the thread we are running in.
        poller = ioThread.getPoller();
    }

    //  When migrating an object from one I/O thread to another, first
    //  unplug it, then migrate it, then plug it to the new thread.
    @Impure
    public final void plug()
    {
        alive = true;
    }

    @Impure
    public final void unplug()
    {
        alive = false;
    }

    @Impure
    public final Handle addFd(SelectableChannel fd)
    {
        return poller.addHandle(fd, this);
    }

    @Impure
    public final void removeHandle(Handle handle)
    {
        poller.removeHandle(handle);
    }

    @Impure
    public final void setPollIn(Handle handle)
    {
        poller.setPollIn(handle);
    }

    @Impure
    public final void setPollOut(Handle handle)
    {
        poller.setPollOut(handle);
    }

    @Impure
    public final void setPollConnect(Handle handle)
    {
        poller.setPollConnect(handle);
    }

    @Impure
    public final void setPollAccept(Handle handle)
    {
        poller.setPollAccept(handle);
    }

    @Impure
    public final void resetPollIn(Handle handle)
    {
        poller.resetPollIn(handle);
    }

    @Impure
    public final void resetPollOut(Handle handle)
    {
        poller.resetPollOut(handle);
    }

    @Impure
    @Override
    public final void inEvent()
    {
        assert (alive);
        handler.inEvent();
    }

    @Impure
    @Override
    public final void outEvent()
    {
        assert (alive);
        handler.outEvent();
    }

    @Impure
    @Override
    public final void connectEvent()
    {
        assert (alive);
        handler.connectEvent();
    }

    @Impure
    @Override
    public final void acceptEvent()
    {
        assert (alive);
        handler.acceptEvent();
    }

    @Impure
    @Override
    public final void timerEvent(int id)
    {
        assert (alive);
        handler.timerEvent(id);
    }

    @Impure
    public final void addTimer(long timeout, int id)
    {
        assert (alive);
        poller.addTimer(timeout, this, id);
    }

    @Impure
    public final void cancelTimer(int id)
    {
        assert (alive);
        poller.cancelTimer(this, id);
    }

    @SideEffectFree
    @Override
    public String toString()
    {
        return String.valueOf(handler);
    }
}
