package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import net.SelectThread.Message.Type;

class SelectThread extends Thread
{
    private Map<SelectionKey,Object> sockets;
    private  Selector selector;
    /**
     * blocking queue of messages sent from the {SelectServer} to the
     *   {SelectThread}.
     */
    private Queue<Message> inMsgq;
    private Queue<Message> outMsgq;
    public SelectThread()
    {
        inMsgq = new LinkedBlockingQueue<>();
        sockets = new LinkedHashMap<>();
    }
    public synchronized void run()
    {
        // open the selector
        try
        {
            selector = Selector.open();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // continuously loop, and select sockets, and deal with them. select
        // may be waken up when things are put into select's threadMsgq.
        // when this occurs, the thread must handle the messages.
        boolean keepLooping = true;
        while(keepLooping)
        {
            // perform select
            int numSelected = 0;
            try
            {
                numSelected = selector.select();
            }

            // bail out, I don't know what other type of exceptions there
            // are
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }

            // get messages from message queue, and handle the messages
            while(inMsgq.size() > 0)
            {
                Message msg = inMsgq.remove();

                switch(msg.type)
                {
                case ADD_SOCKET:
                {
                    Socket sock = (Socket)msg.obj1;
                    InetSocketAddress addr = (InetSocketAddress)msg.obj2;

                    // bind the {ServerSocket} to a port and start listening
                    try
                    {
                        sock.connect(addr);
                    }
                    catch(IOException e)
                    {
                        outMsgq.add(new Message(Type.ON_CONNECT_FAIL,sock,e));
                    }

                    // add the {ServerSocket}'s channel to the selector
                    SelectableChannel channel = sock.getChannel();
                    try
                    {
                        channel.configureBlocking(false);
                        SelectionKey key = channel.register(
                            selector,
                            SelectionKey.OP_ACCEPT|
                            SelectionKey.OP_CONNECT|
                            SelectionKey.OP_READ);
                        sockets.put(key,sock);
                    }
                    catch(IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case RM_SOCKET:
                {
                    Socket sock = (Socket)msg.obj1;
                    sock.close();
                    break;
                }
                /**
                 * open a new server socket to listen to for new
                 *   connections. and adds the {ServerSocket}'s channel to
                 *   to our map of sockets for select to select.
                 */
                case ADD_SERVER_SOCKET:
                {
                    ServerSocket sock = (ServerSocket)msg.obj1;
                    Integer port = (Integer)msg.obj2;

                    // bind the {ServerSocket} to a port and start listening
                    try
                    {
                        sock.bind(new InetSocketAddress(port));
                    }
                    catch(IOException e)
                    {
                        outMsgq.add(new Message(Type.ON_LISTEN_FAIL,sock,e));
                    }

                    // add the {ServerSocket}'s channel to the selector
                    SelectableChannel channel = sock.getChannel();
                    try
                    {
                        channel.configureBlocking(false);
                        SelectionKey key = channel.register(
                            selector,
                            SelectionKey.OP_ACCEPT|
                            SelectionKey.OP_CONNECT|
                            SelectionKey.OP_READ);
                        sockets.put(key,sock);
                    }
                    catch(IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case RM_SERVER_SOCKET:
                    break;
                /**
                 * cancel the select thread, so that it stops.
                 */
                case CANCEL:
                    keepLooping = false;
                    break;
                default:
                    throw new RuntimeException("default case hit");
                }
            }

            // iterate through selected sockets, and handle them
            if(numSelected > 0)
            {
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while(it.hasNext())
                {
                    SelectionKey key = it.next();

                    if(key.isValid())
                    {
                        key.cancel();
                        try
                        {
                            key.channel().close();
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else if(key.isAcceptable())
                    {
                        ServerSocket svrSock = (ServerSocket)sockets.get(key);
                        Socket sock = svrSock.accept();
                        outMsgq.add(new Message(Type.ON_ACCEPT,sock,null));
                    }
                    else if(key.isConnectable())
                    {
                    }
                    else if(key.isReadable())
                    {
                    }

                    // remove the key from the collection because they're
                    // not removed by the selector automatically
                    it.remove();
                }
            }
        }

        // close the selector
        try
        {
            selector.close();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public void addSocket(Socket sock, InetSocketAddress address)
    {
        inMsgq.add(new Message(Type.ADD_SOCKET,sock,address));
        selector.wakeup();
    }
    public void removeSocket(Socket sock)
    {
        inMsgq.add(new Message(Type.RM_SOCKET,sock,null));
        selector.wakeup();
    }
    public void addServerSocket(ServerSocket sock, int serverPort)
    {
        inMsgq.add(new Message(Type.ADD_SERVER_SOCKET,sock,serverPort));
        selector.wakeup();
    }
    public void removeServerSocket(ServerSocket sock)
    {
        inMsgq.add(new Message(Type.RM_SERVER_SOCKET,sock,null));
        selector.wakeup();
    }
    public void cancel()
    {
        inMsgq.add(new Message(Type.CANCEL,null,null));
        selector.wakeup();
    }

    /////////////
    // Message //
    /////////////

    public static class Message
    {
        public enum Type
        {
            ON_ACCEPT,
            ON_CONNECT,
            ON_ACCEPT_FAIL,
            ON_LISTEN_FAIL,
            ON_CONNECT_FAIL,
            ON_MESSAGE,
            ON_CLOSE,

            ADD_SERVER_SOCKET,
            RM_SERVER_SOCKET,
            ADD_SOCKET,
            RM_SOCKET,
            CANCEL
        };

        public final Type type;
        public final Object obj1;
        public final Object obj2;

        public Message(Type type, Object obj1, Object obj2)
        {
            this.type = type;
            this.obj1 = obj1;
            this.obj2 = obj2;
        }
    }
}
