package net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import net.SelectServer.Message.Type;

public class SelectServer implements Server
{
    /**
     * the select thread of this class. isn't instantiated directly, instead,
     *   get the instane's select thread using the getSelectThread method.
     */
    private SelectThread selectThread;

    /**
     * blocking queue of messages sent from the server's internal {SelectThread}
     *   to the {SelectServer}.
     */
    private LinkedBlockingQueue<Message> serverMsgq;

    @Override
    public ServerSocket startListening(int serverPort)
    {
        // open a server socket
        ServerSocket sock;
        try
        {
            sock = new ServerSocket();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        // add the socket to the select thread
        getSelectThread().openServerSocket(sock,serverPort);
        getSelectThread().addChannel(sock.getChannel());

        // return...
        return sock;
    }

    @Override
    public void stopListening(ServerSocket sock)
    {
        // remove the socket from the select thread
        getSelectThread().removeChannel(sock.getChannel());
    }

    @Override
    public void sendMessage(Socket sock, Packet packet)
    {
        synchronized(sock)
        {
            try
            {
                byte[] packetData = packet.toBytes();
                DataOutputStream os = new DataOutputStream(
                    sock.getOutputStream());
                os.writeInt(packetData.length);
                os.write(packetData);
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onAccept(Socket sock)
    {
    }

    @Override
    public void onListenFail(ServerSocket sock, Exception e)
    {
    }

    @Override
    public void onMessage(Socket sock, Packet packet)
    {
    }

    @Override
    public void onClose(Socket sock, boolean remote)
    {
    }

    ///////////////////////
    // private interface //
    ///////////////////////

    private SelectThread getSelectThread()
    {
        if(selectThread == null)
        {
            selectThread = new SelectThread();
        }
        return selectThread;
    }

    /////////////
    // Message //
    /////////////

    public static class Message
    {
        public enum Type
        {
            ON_ACCEPT,
            ON_LISTEN_FAIL,
            ON_MESSAGE,
            ON_CLOSE,

            ADD_CHANNEL,
            RM_CHANNEL,
            START_LISTENING,
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

    //////////////////
    // SelectThread //
    //////////////////

    private class SelectThread extends Thread
    {
        private Set<SelectableChannel> channels;
        private  Selector selector;
        /**
         * blocking queue of messages sent from the {SelectServer} to the
         *   {SelectThread}.
         */
        private LinkedBlockingQueue<Message> threadMsgq;
        public SelectThread()
        {
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
                while(threadMsgq.size() > 0)
                {
                    Message msg = threadMsgq.remove();

                    switch(msg.type)
                    {
                    /**
                     * add a new channel to to our set of channels for select to
                     *   select.
                     */
                    case ADD_CHANNEL:
                        SelectableChannel channel = (SelectableChannel)msg.obj1;
                        try
                        {
                            channel.configureBlocking(false);
                            channel.register(selector,SelectionKey.OP_ACCEPT|
                                SelectionKey.OP_CONNECT|SelectionKey.OP_READ);
                        }
                        catch(IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                        channels.add(channel);
                        break;
                    /**
                     * remove an existing channel from to our set of channels
                     *   for select to select.
                     */
                    case RM_CHANNEL:
                        channels.remove((SelectableChannel)msg.obj1);
                        break;
                    /**
                     * open a new server socket to listen to for new
                     *   connections.
                     */
                    case START_LISTENING:
                    {
                        ServerSocket sock = (ServerSocket)msg.obj1;
                        Integer port = (Integer)msg.obj2;
                        try
                        {
                            sock.bind(new InetSocketAddress(port));
                        }
                        catch(IOException e)
                        {
                            onListenFail(sock,e);
                        }
                        break;
                    }
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
                        }
                        else if(key.isAcceptable())
                        {
                            key.
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
        public void addChannel(SelectableChannel channel)
        {
            threadMsgq.add(new Message(Type.ADD_CHANNEL,channel,null));
            selector.wakeup();
        }
        public void removeChannel(SelectableChannel channel)
        {
            threadMsgq.add(new Message(Type.RM_CHANNEL,channel,null));
            selector.wakeup();
        }
        public void openServerSocket(ServerSocket sock, int serverPort)
        {
            threadMsgq.add(new Message(Type.START_LISTENING,sock,serverPort));
            selector.wakeup();
        }
        public void cancel()
        {
            threadMsgq.add(new Message(Type.CANCEL,null,null));
            selector.wakeup();
        }
    }
}
