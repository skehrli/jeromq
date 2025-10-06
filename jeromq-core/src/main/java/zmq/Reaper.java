package zmq;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.mustcall.qual.NotOwning;
import org.checkerframework.checker.calledmethods.qual.EnsuresCalledMethods;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.checker.mustcall.qual.InheritableMustCall;
import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import zmq.poll.IPollEvents;
import zmq.poll.Poller;

@InheritableMustCall("close")
final class Reaper extends ZObject implements IPollEvents, Closeable
{
    //  Reaper thread accesses incoming commands via this mailbox.
    @Owning
    private final Mailbox mailbox;

    //  Handle associated with mailbox' file descriptor.
    private final Poller.Handle mailboxHandle;

    //  I/O multiplexing is performed using a poller object.
    private final Poller poller;

    //  Number of sockets being reaped at the moment.
    private int socketsReaping;

    //  If true, we were already asked to terminate.
    private final AtomicBoolean terminating = new AtomicBoolean();

    @Impure
    Reaper(Ctx ctx, int tid)
    {
        super(ctx, tid);
        socketsReaping = 0;
        String name = "reaper-" + tid;
        poller = new Poller(ctx, name);

        mailbox = new Mailbox(ctx, name, tid);

        SelectableChannel fd = mailbox.getFd();
        mailboxHandle = poller.addHandle(fd, this);
        poller.setPollIn(mailboxHandle);
    }

    @EnsuresCalledMethods(value="this.mailbox", methods="close")
    @Impure
    @Override
    public void close() throws IOException
    {
        poller.destroy();
        mailbox.close();
    }

    @NotOwning
    @Pure
    Mailbox getMailbox()
    {
        return mailbox;
    }

    @Impure
    void start()
    {
        poller.start();
    }

    @Impure
    void stop()
    {
        if (!terminating.get()) {
            sendStop();
        }
    }

    @Impure
    @Override
    public void inEvent()
    {
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

    @Impure
    @Override
    protected void processStop()
    {
        terminating.set(true);

        //  If there are no sockets being reaped finish immediately.
        if (socketsReaping == 0) {
            finishTerminating();
        }
    }

    @Impure
    @Override
    protected void processReap(SocketBase socket)
    {
        ++socketsReaping;

        //  Add the socket to the poller.
        socket.startReaping(poller);
    }

    @Impure
    @Override
    protected void processReaped()
    {
        --socketsReaping;

        //  If reaped was already asked to terminate and there are no more sockets,
        //  finish immediately.
        if (socketsReaping == 0 && terminating.get()) {
            finishTerminating();
        }
    }

    @Impure
    private void finishTerminating()
    {
        sendDone();
        poller.removeHandle(mailboxHandle);
        poller.stop();
    }
}
