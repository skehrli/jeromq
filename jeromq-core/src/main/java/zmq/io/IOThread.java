package zmq.io;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.mustcall.qual.NotOwning;
import org.checkerframework.checker.calledmethods.qual.EnsuresCalledMethods;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.checker.mustcall.qual.InheritableMustCall;
import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectableChannel;

import zmq.Command;
import zmq.Ctx;
import zmq.Mailbox;
import zmq.ZObject;
import zmq.poll.IPollEvents;
import zmq.poll.Poller;

@InheritableMustCall("close")
public class IOThread extends ZObject implements IPollEvents, Closeable
{
    //  I/O thread accesses incoming commands via this mailbox.
    @Owning
    private final Mailbox mailbox;

    //  Handle associated with mailbox' file descriptor.
    private final Poller.Handle mailboxHandle;

    //  I/O multiplexing is performed using a poller object.
    private final Poller poller;

    @Impure
    public IOThread(Ctx ctx, int tid)
    {
        super(ctx, tid);
        String name = "iothread-" + tid;
        poller = new Poller(ctx, name);

        mailbox = new Mailbox(ctx, name, tid);
        SelectableChannel fd = mailbox.getFd();
        mailboxHandle = poller.addHandle(fd, this);
        poller.setPollIn(mailboxHandle);
    }

    @Impure
    public void start()
    {
        poller.start();
    }

    @EnsuresCalledMethods(value="this.mailbox", methods="close")
    @Impure
    @Override
    public void close() throws IOException
    {
        poller.destroy();
        mailbox.close();
    }

    @Impure
    public void stop()
    {
        sendStop();
    }

    @NotOwning
    @Pure
    public Mailbox getMailbox()
    {
        return mailbox;
    }

    @Impure
    public int getLoad()
    {
        return poller.getLoad();
    }

    @Impure
    @Override
    public void inEvent()
    {
        //  TODO: Do we want to limit number of commands I/O thread can
        //  process in a single go?

        while (true) {
            //  Get the next command. If there is none, exit.
            Command cmd = mailbox.recv(0);
            if (cmd == null) {
                break;
            }

            //  Process the command.
            cmd.process();
        }
    }

    @Pure
    Poller getPoller()
    {
        assert (poller != null);
        return poller;
    }

    @Impure
    @Override
    protected void processStop()
    {
        poller.removeHandle(mailboxHandle);

        poller.stop();
    }
}
