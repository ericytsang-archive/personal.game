package net;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.SelectThread.Message.Type;

class SelectThread extends Thread
{
    private  Selector selector;

    private BiMap<SelectionKey,Object> sockets;

    private BiMap<Object,SelectionKey> keys;

    /**
     * blocking queue of messages sent from the {SelectServer} to the
     *   {SelectThread}.
     */
    private Queue<Message> inMsgq;

    private Queue<Message> outMsgq;

    /////////////////
    // constructor //
    /////////////////

    public SelectThread()
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

        sockets = HashBiMap.create(1024);
        keys = sockets.inverse();
        inMsgq = new LinkedBlockingQueue<>();
        outMsgq = new LinkedBlockingQueue<>();
    }

    /////////////
    // methods //
    /////////////

    public synchronized void run()
    {
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
                case CONNECT:
                    handleAddSocket(msg);
                    break;
                case DISCONNECT:
                    handleRemoveSocket(msg);
                    break;
                case START_LISTEN:
                    handleAddServerSocket(msg);
                    break;
                case STOP_LISTEN:
                    handleRemoveServerSocket(msg);
                    break;
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
                        handleOnCloseable(key);
                    else if(key.isReadable())
                        handleOnReadable(key);
                    else if(key.isConnectable())
                        handleOnConnectable(key);
                    else if(key.isAcceptable())
                        handleOnAcceptable(key);

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

    public void handleMessages(SelectListsner listener)
    {
        while(outMsgq.size() > 0)
        {
            Message msg = outMsgq.remove();

            switch(msg.type)
            {
            case ON_ACCEPT:
                listener.onAccept((Socket)msg.obj1);
                break;
            case ON_CONNECT:
                listener.onListenFail((ServerSocket)msg.obj1,(Exception)msg.obj2);
                break;
            case ON_ACCEPT_FAIL:
                listener.onConnect((Socket)msg.obj1);
                break;
            case ON_LISTEN_FAIL:
                listener.onAcceptFail((ServerSocket)msg.obj1,(Exception)msg.obj2);
                break;
            case ON_CONNECT_FAIL:
                listener.onConnectFail((Socket)msg.obj1,(Exception)msg.obj2);
                break;
            case ON_MESSAGE:
                listener.onMessage((Socket)msg.obj1,(Packet)msg.obj2);
                break;
            case ON_CLOSE:
                listener.onClose((Socket)msg.obj1,(boolean)msg.obj2);
                break;
            default:
                throw new RuntimeException("default case hit");
            }
        }
    }

    ////////////////////////////////////////////////////
    // methods below enqueue messages into the inMsgq //
    ////////////////////////////////////////////////////

    public Socket connect(String remoteName, int remotePort)
    {
        Socket sock = new Socket();
        Object address = new InetSocketAddress(remoteName,remotePort);
        inMsgq.add(new Message(Type.CONNECT,sock,address));
        selector.wakeup();
        return sock;
    }

    public void disconnect(Socket sock)
    {
        inMsgq.add(new Message(Type.DISCONNECT,sock,null));
        selector.wakeup();
    }

    public ServerSocket startListening(int serverPort)
    {
        try
        {
            ServerSocket sock = new ServerSocket();
            inMsgq.add(new Message(Type.START_LISTEN,sock,serverPort));
            selector.wakeup();
            return sock;
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void stopListening(ServerSocket sock)
    {
        inMsgq.add(new Message(Type.STOP_LISTEN,sock,null));
        selector.wakeup();
    }

    public void cancel()
    {
        inMsgq.add(new Message(Type.CANCEL,null,null));
        selector.wakeup();
    }

    ///////////////////////////////////////////////////////////
    // methods below dequeue and handle messages from inMsgq //
    ///////////////////////////////////////////////////////////

    private void handleAddSocket(Message msg)
    {
        Socket sock = (Socket)msg.obj1;
        InetSocketAddress addr = (InetSocketAddress)msg.obj2;

        // add the {Socket}'s channel to the selector
        SelectableChannel channel = sock.getChannel();
        try
        {
            channel.configureBlocking(false);
            SelectionKey key = channel.register(
                selector,
                SelectionKey.OP_CONNECT|
                SelectionKey.OP_READ);
            sockets.put(key,sock);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }

        // connect the {Socket} to the remote host
        try
        {
            sock.getChannel().connect(addr);
        }
        catch(IOException e)
        {
            outMsgq.add(new Message(Type.ON_CONNECT_FAIL,sock,e));
        }
    }

    private void handleRemoveSocket(Message msg)
    {
        Socket sock = (Socket)msg.obj1;
        SelectionKey key = keys.get(sock);

        try
        {
            sock.close();
            key.cancel();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void handleAddServerSocket(Message msg)
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
                SelectionKey.OP_ACCEPT);
            sockets.put(key,sock);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void handleRemoveServerSocket(Message msg)
    {
        ServerSocket sock = (ServerSocket)msg.obj1;
        SelectionKey key = keys.get(sock);

        try
        {
            sock.close();
            key.cancel();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    //////////////////////////////////////////////////
    // methods below handle keys signaled by select //
    //////////////////////////////////////////////////

    private void handleOnAcceptable(SelectionKey key)
    {
        ServerSocket sock = (ServerSocket) sockets.get(key);
        try
        {
            sock.accept();
            outMsgq.add(new Message(Type.ON_ACCEPT,sock,null));
        }
        catch(Exception e)
        {
            outMsgq.add(new Message(Type.ON_ACCEPT_FAIL,sock,e));
        }
    }

    private void handleOnConnectable(SelectionKey key)
    {
        Socket sock = (Socket) sockets.get(key);
        SocketChannel channel = (SocketChannel) key.channel();

        try
        {
            channel.finishConnect();
            outMsgq.add(new Message(Type.ON_CONNECT,sock,null));
        }
        catch(IOException e)
        {
            outMsgq.add(new Message(Type.ON_CONNECT_FAIL,sock,e));
        }
    }

    private void handleOnCloseable(SelectionKey key)
    {
        key.cancel();
        try
        {
            key.channel().close();
            outMsgq.add(new Message(Type.ON_CLOSE,sockets.get(key),null));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void handleOnReadable(SelectionKey key)
    {
        Socket sock = (Socket) sockets.get(key);
        try
        {
            DataInputStream is = new DataInputStream(sock.getInputStream());
            byte[] packetData = new byte[is.readInt()];
            is.read(packetData);
            Packet packet = new Packet().fromBytes(packetData);
            outMsgq.add(new Message(Type.ON_MESSAGE,sock,packet));
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    ////////////////////
    // SelectListener //
    ////////////////////

    public interface SelectListsner
    {
        public abstract void onAccept(Socket sock);
        public abstract void onListenFail(ServerSocket sock, Exception e);
        public abstract void onConnect(Socket conn);
        public abstract void onAcceptFail(ServerSocket sock, Exception e);
        public abstract void onConnectFail(Socket conn, Exception e);
        public abstract void onMessage(Socket sock, Packet packet);
        public abstract void onClose(Socket sock, boolean remote);
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

            START_LISTEN,
            STOP_LISTEN,
            CONNECT,
            DISCONNECT,
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

    //////////
    // main //
    //////////

    public static void main(String[] args)
    {
        SelectThread st1 = new SelectThread();
        SelectThread st2 = new SelectThread();
        st1.start();
        st2.start();

        SelectListsner listener = new SelectListsner()
        {
            @Override
            public void onAccept(Socket sock)
            {
                System.out.println("onAccept");
            }
            @Override
            public void onListenFail(ServerSocket sock, Exception e)
            {
                System.out.println("onListenFail");
            }
            @Override
            public void onConnect(Socket conn)
            {
                System.out.println("onConnect");
            }
            @Override
            public void onAcceptFail(ServerSocket sock, Exception e)
            {
                System.out.println("onAcceptFail");
            }
            @Override
            public void onConnectFail(Socket conn, Exception e)
            {
                System.out.println("onConnectFail");
            }
            @Override
            public void onMessage(Socket sock, Packet packet)
            {
                System.out.println("onMessage");
            }
            @Override
            public void onClose(Socket sock, boolean remote)
            {
                System.out.println("onClose");
            }
        };

        st2.startListening(7000);
        st1.connect("localhost",7000);

        st1.handleMessages(listener);
        st2.handleMessages(listener);
    }
}
