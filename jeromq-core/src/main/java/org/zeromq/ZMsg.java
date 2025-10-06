package org.zeromq;

import org.checkerframework.framework.qual.EnsuresQualifier;
import org.checkerframework.checker.collectionownership.qual.PolyOwningCollection;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.zeromq.ZMQ.Socket;

import zmq.util.Draft;
import zmq.util.function.Consumer;

/**
 * The ZMsg class provides methods to send and receive multipart messages
 * across 0MQ sockets. This class provides a list-like container interface,
 * with methods to work with the overall container.  ZMsg messages are
 * composed of zero or more ZFrame objects.
 *
 * <pre>
 * // Send a simple single-frame string message on a ZMQSocket "output" socket object
 * ZMsg.newStringMsg("Hello").send(output);
 *
 * // Add several frames into one message
 * ZMsg msg = new ZMsg();
 * for (int i = 0 ; i &lt; 10 ; i++) {
 *     msg.addString("Frame" + i);
 * }
 * msg.send(output);
 *
 * // Receive message from ZMQSocket "input" socket object and iterate over frames
 * ZMsg receivedMessage = ZMsg.recvMsg(input);
 * for (ZFrame f : receivedMessage) {
 *     // Do something with frame f (of type ZFrame)
 * }
 * </pre>
 *
 * Based on <a href="http://github.com/zeromq/czmq/blob/master/src/zmsg.c">zmsg.c</a> in czmq
 *
 */

public class ZMsg implements Iterable<ZFrame>, Deque<ZFrame>
{
    /**
     * Hold internal list of ZFrame objects
     */
    private final ArrayDeque<ZFrame> frames = new ArrayDeque<>();

    /**
     * Class Constructor
     */
    @SideEffectFree
    public ZMsg()
    {
    }

    /**
     * Destructor.
     * Explicitly destroys all ZFrames contains in the ZMsg
     */
    @Impure
    public void destroy()
    {
        for (ZFrame f : frames) {
            f.destroy();
        }
        frames.clear();
    }

    /**
     * @return total number of bytes contained in all ZFrames in this ZMsg
     */
    @Pure
    @Impure
    public long contentSize()
    {
        long size = 0;
        for (ZFrame f : frames) {
            size += f.size();
        }
        return size;
    }

    /**
     * Add a String as a new ZFrame to the end of list
     * @param str
     *              String to add to list
     */
    @Impure
    public ZMsg addString(String str)
    {
        frames.add(new ZFrame(str));
        return this;
    }

    /**
     * Creates copy of this ZMsg.
     * Also duplicates all frame content.
     * @return
     *          The duplicated ZMsg object, else null if this ZMsg contains an empty frame set
     */
    @Impure
    public ZMsg duplicate()
    {
        if (frames.isEmpty()) {
            return null;
        }
        else {
            ZMsg msg = new ZMsg();
            for (ZFrame f : frames) {
                msg.add(f.duplicate());
            }
            return msg;
        }
    }

    /**
     * Push frame plus empty frame to front of message, before 1st frame.
     * Message takes ownership of frame, will destroy it when message is sent.
     * @param frame
     */
    @Impure
    public ZMsg wrap(ZFrame frame)
    {
        if (frame != null) {
            push(new ZFrame(""));
            push(frame);
        }
        return this;
    }

    /**
     * Pop frame off front of message, caller now owns frame.
     * If next frame is empty, pops and destroys that empty frame
     * (e.g. useful when unwrapping ROUTER socket envelopes)
     * @return
     *          Unwrapped frame
     */
    @Impure
    public ZFrame unwrap()
    {
        if (size() == 0) {
            return null;
        }
        ZFrame f = pop();
        ZFrame empty = getFirst();
        if (empty.hasData() && empty.size() == 0) {
            empty = pop();
            empty.destroy();
        }
        return f;
    }

    /**
     * Send message to 0MQ socket.
     *
     * @param socket
     *              0MQ socket to send ZMsg on.
     * @return true if send is success, false otherwise
     */
    @Impure
    public boolean send(Socket socket)
    {
        return send(socket, true);
    }

    /**
     * Send message to 0MQ socket, destroys contents after sending if destroy param is set to true.
     * If the message has no frames, sends nothing but still destroy()s the ZMsg object
     * @param socket
     *              0MQ socket to send ZMsg on.
     * @return true if send is success, false otherwise
     */
    @Impure
    public boolean send(Socket socket, boolean destroy)
    {
        if (socket == null) {
            throw new IllegalArgumentException("socket is null");
        }

        if (frames.isEmpty()) {
            return true;
        }

        boolean ret = true;
        Iterator<ZFrame> i = frames.iterator();
        while (i.hasNext()) {
            ZFrame f = i.next();
            ret = f.sendAndKeep(socket, (i.hasNext()) ? ZFrame.MORE : 0);
            if (!ret) {
                break;
            }
        }
        if (destroy) {
            destroy();
        }
        return ret;
    }

    /**
     * Receives message from socket, returns ZMsg object or null if the
     * recv was interrupted. Does a blocking recv, if you want not to block then use
     * the ZLoop class or ZMQ.Poller to check for socket input before receiving or recvMsg with flag ZMQ.DONTWAIT.
     * @param   socket
     * @return
     *          ZMsg object, null if interrupted
     */
    @Impure
    public static ZMsg recvMsg(Socket socket)
    {
        return recvMsg(socket, 0);
    }

    /**
     * Receives message from socket, returns ZMsg object or null if the
     * recv was interrupted.
     * @param   socket
     * @param   wait true to wait for next message, false to do a non-blocking recv.
     * @return
     *          ZMsg object, null if interrupted
     */
    @Impure
    public static ZMsg recvMsg(Socket socket, boolean wait)
    {
        return recvMsg(socket, wait ? 0 : ZMQ.DONTWAIT);
    }

    /**
     * Receives message from socket, returns ZMsg object or null if the
     * recv was interrupted. Setting the flag to ZMQ.DONTWAIT does a non-blocking recv.
     * @param   socket
     * @param   flag see ZMQ constants
     * @return
     *          ZMsg object, null if interrupted
     */
    @Impure
    public static ZMsg recvMsg(Socket socket, int flag)
    {
        if (socket == null) {
            throw new IllegalArgumentException("socket is null");
        }

        ZMsg msg = new ZMsg();

        while (true) {
            ZFrame f = ZFrame.recvFrame(socket, flag);
            if (f == null) {
                // If receive failed or was interrupted
                msg.destroy();
                msg = null;
                break;
            }
            msg.add(f);
            if (!f.hasMore()) {
                break;
            }
        }
        return msg;
    }

    /**
     * This API is in DRAFT state and is subject to change at ANY time until declared stable
     * handle incoming message with a handler
     *
     * @param socket
     * @param flags see ZMQ constants
     * @param handler handler to handle incoming message
     * @param exceptionHandler handler to handle exceptions
     */
    @Impure
    @Draft
    public static void recvMsg(ZMQ.Socket socket, int flags,
                               Consumer<ZMsg> handler,
                               Consumer<ZMQException> exceptionHandler)
    {
        try {
            handler.accept(ZMsg.recvMsg(socket, flags));
        }
        catch (ZMQException e) {
            exceptionHandler.accept(e);
        }
    }

    /**
     * This API is in DRAFT state and is subject to change at ANY time until declared stable
     * handle incoming message with a handler
     *
     * @param socket
     * @param flags see ZMQ constants
     * @param handler handler to handle incoming message
     */
    @Impure
    @Draft
    public static void recvMsg(ZMQ.Socket socket, int flags, Consumer<ZMsg> handler)
    {
        handler.accept(ZMsg.recvMsg(socket, flags));
    }

    /**
     * Save message to an open data output stream.
     * <p>
     * Data saved as:
     *      4 bytes: number of frames
     *  For every frame:
     *      4 bytes: byte size of frame data
     *      + n bytes: frame byte data
     *
     * @param msg
     *          ZMsg to save
     * @param file
     *          DataOutputStream
     * @return
     *          True if saved OK, else false
     */
    @Impure
    public static boolean save(ZMsg msg, DataOutputStream file)
    {
        if (msg == null) {
            return false;
        }

        try {
            // Write number of frames
            file.writeInt(msg.size());
            if (msg.size() > 0) {
                for (ZFrame f : msg) {
                    // Write byte size of frame
                    file.writeInt(f.size());
                    // Write frame byte data
                    file.write(f.getData());
                }
            }
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Load / append a ZMsg from an open DataInputStream
     *
     * @param file
     *          DataInputStream connected to file
     * @return
     *          ZMsg object
     */
    @Impure
    public static ZMsg load(DataInputStream file)
    {
        if (file == null) {
            return null;
        }
        ZMsg rcvMsg = new ZMsg();

        try {
            int msgSize = file.readInt();
            if (msgSize > 0) {
                int msgNbr = 0;
                while (++msgNbr <= msgSize) {
                    int frameSize = file.readInt();
                    byte[] data = new byte[frameSize];
                    file.read(data);
                    rcvMsg.add(new ZFrame(data));
                }
            }
            return rcvMsg;
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * Create a new ZMsg from one or more Strings
     *
     * @param strings
     *      Strings to add as frames.
     * @return
     *      ZMsg object
     */
    @Impure
    public static ZMsg newStringMsg(String... strings)
    {
        ZMsg msg = new ZMsg();
        for (String data : strings) {
            msg.addString(data);
        }
        return msg;
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.NotOwningCollection.class)
    @Impure
    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZMsg zMsg = (ZMsg) o;

        //based on AbstractList
        Iterator<ZFrame> e1 = frames.iterator();
        Iterator<ZFrame> e2 = zMsg.frames.iterator();
        while (e1.hasNext() && e2.hasNext()) {
            ZFrame o1 = e1.next();
            ZFrame o2 = e2.next();
            if (!(Objects.equals(o1, o2))) {
                return false;
            }
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.NotOwningCollection.class)
    @Pure
    @Override
    public int hashCode()
    {
        if (frames.isEmpty()) {
            return 0;
        }

        int result = 1;
        for (ZFrame frame : frames) {
            result = 31 * result + (frame == null ? 0 : frame.hashCode());
        }

        return result;
    }

    /**
     * Dump the message in human readable format. This should only be used
     * for debugging and tracing, inefficient in handling large messages.
     **/
    @Impure
    public ZMsg dump(Appendable out)
    {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.printf("--------------------------------------\n");
            for (ZFrame frame : frames) {
                pw.printf("[%03d] %s\n", frame.size(), frame);
            }
            out.append(sw.getBuffer());
            sw.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Message dump exception " + super.toString(), e);
        }
        return this;
    }

    @Impure
    public ZMsg dump()
    {
        dump(System.out);
        return this;
    }

    // ********* Convenience Deque methods for common data types *** //

    @Impure
    public ZMsg addFirst(String stringValue)
    {
        addFirst(new ZFrame(stringValue));
        return this;
    }

    @Impure
    public ZMsg addFirst(byte[] data)
    {
        addFirst(new ZFrame(data));
        return this;
    }

    @Impure
    public ZMsg addLast(String stringValue)
    {
        addLast(new ZFrame(stringValue));
        return this;
    }

    @Impure
    public ZMsg addLast(byte[] data)
    {
        addLast(new ZFrame(data));
        return this;
    }

    // ********* Convenience Queue methods for common data types *** //

    @Impure
    public ZMsg push(String str)
    {
        push(new ZFrame(str));
        return this;
    }

    @Impure
    public ZMsg push(byte[] data)
    {
        push(new ZFrame(data));
        return this;
    }

    @Impure
    public boolean add(String stringValue)
    {
        return add(new ZFrame(stringValue));
    }

    @Impure
    public boolean add(byte[] data)
    {
        return add(new ZFrame(data));
    }

    /**
     * Adds a string as a new frame in the message.
     *
     * @param stringValue the value to add
     * @return this
     */
    @Impure
    public ZMsg append(String stringValue)
    {
        add(stringValue);
        return this;
    }

    /**
     * Adds bytes as a new frame in the message.
     *
     * @param data the value to add
     * @return this
     */
    @Impure
    public ZMsg append(byte[] data)
    {
        add(data);
        return this;
    }

    // ********* Implement Iterable Interface *************** //
    @SideEffectFree
    @Override
    public Iterator<ZFrame> iterator(@PolyOwningCollection ZMsg this)
    {
        return frames.iterator();
    }

    // ********* Implement Deque Interface ****************** //
    @Impure
    @Override
    public boolean addAll(Collection<? extends ZFrame> arg0)
    {
        return frames.addAll(arg0);
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.OwningCollectionWithoutObligation.class)
    @Impure
    @Override
    public void clear()
    {
        frames.clear();
    }

    @Pure
    @Override
    public boolean containsAll(Collection<?> arg0)
    {
        return frames.containsAll(arg0);
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.NotOwningCollection.class)
    @Pure
    @Override
    public boolean isEmpty()
    {
        return frames.isEmpty();
    }

    @Impure
    @Override
    public boolean removeAll(Collection<?> arg0)
    {
        return frames.removeAll(arg0);
    }

    @Impure
    @Override
    public boolean retainAll(Collection<?> arg0)
    {
        return frames.retainAll(arg0);
    }

    @SideEffectFree
    @Override
    public Object[] toArray()
    {
        return frames.toArray();
    }

    @SideEffectFree
    @Override
    public <T> T[] toArray(T[] arg0)
    {
        return frames.toArray(arg0);
    }

    @Impure
    @Override
    public boolean add(ZFrame e)
    {
        return frames.add(e);
    }

    @Impure
    @Override
    public void addFirst(ZFrame e)
    {
        frames.addFirst(e);
    }

    @Impure
    @Override
    public void addLast(ZFrame e)
    {
        frames.addLast(e);
    }

    @Pure
    @Override
    public boolean contains(Object o)
    {
        return frames.contains(o);
    }

    @Impure
    @Override
    public Iterator<ZFrame> descendingIterator()
    {
        return frames.descendingIterator();
    }

    @Impure
    @Override
    public ZFrame element()
    {
        return frames.element();
    }

    @Pure
    @Override
    public ZFrame getFirst()
    {
        return frames.peekFirst();
    }

    @Pure
    @Override
    public ZFrame getLast()
    {
        return frames.peekLast();
    }

    @Impure
    @Override
    public boolean offer(ZFrame e)
    {
        return frames.offer(e);
    }

    @Impure
    @Override
    public boolean offerFirst(ZFrame e)
    {
        return frames.offerFirst(e);
    }

    @Impure
    @Override
    public boolean offerLast(ZFrame e)
    {
        return frames.offerLast(e);
    }

    @Pure
    @Override
    public ZFrame peek()
    {
        return frames.peek();
    }

    @Pure
    @Override
    public ZFrame peekFirst()
    {
        return frames.peekFirst();
    }

    @Pure
    @Override
    public ZFrame peekLast()
    {
        return frames.peekLast();
    }

    @Impure
    @Override
    public ZFrame poll()
    {
        return frames.poll();
    }

    @Impure
    @Override
    public ZFrame pollFirst()
    {
        return frames.pollFirst();
    }

    @Impure
    @Override
    public ZFrame pollLast()
    {
        return frames.pollLast();
    }

    @Impure
    @Override
    public ZFrame pop()
    {
        return frames.poll();
    }

    /**
     * Pop a ZFrame and return the toString() representation of it.
     *
     * @return toString version of pop'ed frame, or null if no frame exists.
     */
    @Impure
    public String popString()
    {
        ZFrame frame = pop();
        if (frame == null) {
            return null;
        }

        return frame.toString();
    }

    @Impure
    @Override
    public void push(ZFrame e)
    {
        frames.push(e);
    }

    @Impure
    @Override
    public ZFrame remove()
    {
        return frames.remove();
    }

    @Impure
    @Override
    public boolean remove(Object o)
    {
        return frames.remove(o);
    }

    @Impure
    @Override
    public ZFrame removeFirst()
    {
        return frames.pollFirst();
    }

    @Impure
    @Override
    public boolean removeFirstOccurrence(Object o)
    {
        return frames.removeFirstOccurrence(o);
    }

    @Impure
    @Override
    public ZFrame removeLast()
    {
        return frames.pollLast();
    }

    @Impure
    @Override
    public boolean removeLastOccurrence(Object o)
    {
        return frames.removeLastOccurrence(o);
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.NotOwningCollection.class)
    @Pure
    @Override
    public int size()
    {
        return frames.size();
    }

    @Impure
    public ZMsg append(ZMsg msg)
    {
        if (msg == null) {
            return this;
        }
        // Tests explicitly check appending a ZMsg to itself, protect that
        if (msg != this) {
            frames.addAll(msg.frames);
        }
        else {
            frames.addAll(msg.frames.clone());
        }
        return this;
    }

    /**
     * Returns pretty string representation of multipart message:
     * [ frame0, frame1, ..., frameN ]
     *
     * @return toString version of ZMsg object
     */
    @Impure
    @Override
    public String toString()
    {
        String joined = frames.stream().map(ZFrame::toString).collect(Collectors.joining(", "));
        return new StringBuilder("[ ").append(joined).append(" ]").toString();
    }
}
