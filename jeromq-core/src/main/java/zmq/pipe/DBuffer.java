package zmq.pipe;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.checker.mustcall.qual.NotOwning;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.dataflow.qual.Pure;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import zmq.Msg;

class DBuffer<T extends Msg>
{
    private T back;
    private T front;

    private final Lock sync = new ReentrantLock();

    private boolean hasMsg;

    @NotOwning
    @Pure
    public T back()
    {
        return back;
    }

    @NotOwning
    @Pure
    public T front()
    {
        return front;
    }

    @Impure
    void write(@Owning T msg)
    {
        assert (msg.check());
        sync.lock();
        try {
            back = front;
            front = msg;
            hasMsg = true;
        }
        finally {
            sync.unlock();
        }
    }

    @NotOwning
    @Impure
    T read()
    {
        sync.lock();
        try {
            if (!hasMsg) {
                return null;
            }

            assert (front.check());
            // TODO front->init ();     // avoid double free
            hasMsg = false;

            return front;
        }
        finally {
            sync.unlock();
        }
    }

    @Impure
    boolean checkRead()
    {
        sync.lock();
        try {
            return hasMsg;
        }
        finally {
            sync.unlock();
        }
    }

    @NotOwning
    @Impure
    T probe()
    {
        sync.lock();
        try {
            return front;
        }
        finally {
            sync.unlock();
        }
    }
}
