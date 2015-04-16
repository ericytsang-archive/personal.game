package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import net.SelectThread.Message.Type;

class SelectThread extends Thread
{
    private  Selector selector;

    /**
     * blocking queue of messages sent from the {SelectServer} to the
     *   {SelectThread}.
     */
    private Queue<Message> inMsgq;

    private Queue<Message> outMsgq;

    //////////////////
    // constructors //
    //////////////////

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

        inMsgq = new LinkedBlockingQueue<>();
        outMsgq = new LinkedBlockingQueue<>();
    }

    //////////////////////
    // public interface //
    //////////////////////

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
                    handleConnect(msg);
                    break;
                case DISCONNECT:
                    handleDisconnect(msg);
                    break;
                case START_LISTEN:
                    handleStartListening(msg);
                    break;
                case STOP_LISTEN:
                    handleStopListening(msg);
                    break;
                case SEND_MESSAGE:
                    handleSendMessage(msg);
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

                    if(key.isReadable())
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
                listener.onAccept((SocketChannel)msg.obj1);
                break;
            case ON_CONNECT:
                listener.onConnect((SocketChannel)msg.obj1);
                break;
            case ON_ACCEPT_FAIL:
                listener.onAcceptFail((ServerSocketChannel)msg.obj1,(Exception)msg.obj2);
                break;
            case ON_LISTEN_FAIL:
                listener.onListenFail((ServerSocketChannel)msg.obj1,(Exception)msg.obj2);
                break;
            case ON_CONNECT_FAIL:
                listener.onConnectFail((SocketChannel)msg.obj1,(Exception)msg.obj2);
                break;
            case ON_MESSAGE:
                listener.onMessage((SocketChannel)msg.obj1,(Packet)msg.obj2);
                break;
            case ON_CLOSE:
                listener.onClose((SocketChannel)msg.obj1,(boolean)msg.obj2);
                try
                {
                    ((SocketChannel)msg.obj1).close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                break;
            default:
                throw new RuntimeException("default case hit");
            }
        }
    }

    // methods below enqueue messages into the inMsgq

    public SocketChannel connect(String remoteName, int remotePort)
    {
        try
        {
            SocketChannel channel = SocketChannel.open();
            InetSocketAddress addr = new InetSocketAddress(remoteName,remotePort);
            inMsgq.add(new Message(Type.CONNECT,channel,addr));
            selector.wakeup();
            return channel;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void disconnect(SocketChannel channel)
    {
        inMsgq.add(new Message(Type.DISCONNECT,channel,null));
        selector.wakeup();
    }

    public ServerSocketChannel startListening(int serverPort)
    {
        try
        {
            ServerSocketChannel channel = ServerSocketChannel.open();
            InetSocketAddress addr = new InetSocketAddress(serverPort);
            inMsgq.add(new Message(Type.START_LISTEN,channel,addr));
            selector.wakeup();
            return channel;
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void stopListening(ServerSocketChannel channel)
    {
        inMsgq.add(new Message(Type.STOP_LISTEN,channel,null));
        selector.wakeup();
    }

    public void sendMessage(SocketChannel channel, Packet packet)
    {
        inMsgq.add(new Message(Type.SEND_MESSAGE,channel,packet));
        selector.wakeup();
    }

    public void sendMessageOnThisThread(SocketChannel channel, Packet packet)
    {
        handleSendMessage(new Message(Type.SEND_MESSAGE,channel,packet));
    }

    public void cancel()
    {
        inMsgq.add(new Message(Type.CANCEL,null,null));
        selector.wakeup();
    }

    ///////////////////////
    // private interface //
    ///////////////////////

    // methods below are general helper methods

    private void registerChannel(SocketChannel channel)
    {
        // add the {Socket}'s channel to the selector
        try
        {
            channel.configureBlocking(false);
            channel.register(
                selector,SelectionKey.OP_CONNECT|SelectionKey.OP_READ);
        }

        // should not fail unless dumb; bail out
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void registerChannel(ServerSocketChannel channel)
    {
        // add the {Socket}'s channel to the selector
        try
        {
            channel.configureBlocking(false);
            channel.register(
                selector,SelectionKey.OP_ACCEPT);
        }

        // should not fail unless dumb; bail out
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void unregisterChannel(SelectableChannel channel)
    {
        // parse message parameters
        SelectionKey key = channel.keyFor(selector);

        // cancel the selection key, and closes the socket channel
        try
        {
            key.cancel();
            channel.close();
        }

        // failed because dumb; bail out
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    // methods below dequeue and handle messages from inMsgq

    private void handleConnect(Message msg)
    {
        // parse message parameters
        SocketChannel channel = (SocketChannel)msg.obj1;
        InetSocketAddress addr = (InetSocketAddress)msg.obj2;

        // add the {Socket}'s channel to the selector
        registerChannel(channel);

        // connect the {Socket} to the remote host
        try
        {
            channel.connect(addr);
        }

        // failed to connect; put message in outMsgq to invoke callback
        catch(IOException e)
        {
            outMsgq.add(new Message(Type.ON_CONNECT_FAIL,channel,e));
        }
    }

    private void handleDisconnect(Message msg)
    {
        // parse message parameters
        SocketChannel channel = (SocketChannel)msg.obj1;

        // cancel the selection key, and closes the socket channel
        channel.keyFor(selector).cancel();

        // closed connection; enqueue into outMsgq to invoke callback
        outMsgq.add(new Message(Type.ON_CLOSE,channel,false));
    }

    private void handleStartListening(Message msg)
    {
        // parse message parameters
        ServerSocketChannel channel = (ServerSocketChannel)msg.obj1;
        InetSocketAddress addr = (InetSocketAddress)msg.obj2;

        // add the channel to the selector
        registerChannel(channel);

        // bind the channel to a port and start listening
        try
        {
            channel.bind(addr);
        }

        // failed to bind; put message in outMsgq to invoke callback
        catch(IOException e)
        {
            outMsgq.add(new Message(Type.ON_LISTEN_FAIL,channel,e));
        }
    }

    private void handleStopListening(Message msg)
    {
        // parse message parameters
        ServerSocketChannel channel = (ServerSocketChannel)msg.obj1;

        // cancel the selection key, and closes the socket channel
        unregisterChannel(channel);
    }

    public void handleSendMessage(Message msg)
    {
        // parse message parameters
        SocketChannel channel = (SocketChannel)msg.obj1;
        Packet packet = (Packet)msg.obj2;

        // send the message out the channel
        synchronized(channel)
        {
            try
            {
                byte[] packetData = packet.toBytes();
                ByteBuffer buf = ByteBuffer.allocate(4+packetData.length);
                buf.putInt(packetData.length);
                buf.put(packetData);
                buf.position(0);
                channel.write(buf);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    // methods below handle keys signaled by select

    private void handleOnAcceptable(SelectionKey key)
    {
        // parse message parameters
        ServerSocketChannel channel = (ServerSocketChannel)key.channel();

        // try to accept the new connection
        try
        {
            // accept the new connection
            SocketChannel acceptedChannel = channel.accept();

            // accepted; put message in outMsgq to invoke callback
            outMsgq.add(new Message(Type.ON_ACCEPT,acceptedChannel,null));

            // add the {Socket}'s channel to the selector
            registerChannel(acceptedChannel);
        }

        // failed to accept; put message in outMsgq to invoke callback
        catch(Exception e)
        {
            outMsgq.add(new Message(Type.ON_ACCEPT_FAIL,channel,e));
        }
    }

    private void handleOnConnectable(SelectionKey key)
    {
        // parse message parameters
        SocketChannel channel = (SocketChannel)key.channel();

        // try to finish the connection
        try
        {
            channel.finishConnect();
            outMsgq.add(new Message(Type.ON_CONNECT,channel,null));
        }

        // failed to connect; put message in outMsgq to invoke callback
        catch(IOException e)
        {
            outMsgq.add(new Message(Type.ON_CONNECT_FAIL,channel,e));
        }
    }

    private void handleOnReadable(SelectionKey key)
    {
        // parse message parameters
        SocketChannel channel = (SocketChannel)key.channel();

        try
        {
            ByteBuffer len = ByteBuffer.allocate(4);
            if(channel.read(len) != -1)
            {
                len.position(0);
                int adsf = len.getInt();
                ByteBuffer packetData = ByteBuffer.allocate(adsf);
                channel.read(packetData);
                packetData.position(0);
                Packet packet = new Packet().fromBytes(packetData.array());
                outMsgq.add(new Message(Type.ON_MESSAGE,channel,packet));
            }
            else
            {
                channel.keyFor(selector).cancel();
                outMsgq.add(new Message(Type.ON_CLOSE,channel,true));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    ////////////////////
    // SelectListener //
    ////////////////////

    public interface SelectListsner
    {
        public abstract void onAccept(SocketChannel chnl);
        public abstract void onConnect(SocketChannel chnl);
        public abstract void onAcceptFail(ServerSocketChannel chnl, Exception e);
        public abstract void onListenFail(ServerSocketChannel chnl, Exception e);
        public abstract void onConnectFail(SocketChannel chnl, Exception e);
        public abstract void onMessage(SocketChannel chnl, Packet packet);
        public abstract void onClose(SocketChannel chnl, boolean remote);
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
            SEND_MESSAGE,
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
        final SelectThread st1 = new SelectThread();
        final SelectThread st2 = new SelectThread();
        st1.start();
        st2.start();

        SelectListsner listener = new SelectListsner()
        {
            @Override
            public void onAccept(SocketChannel chnl)
            {
                System.out.println(chnl+": onAccept");
            }
            @Override
            public void onListenFail(ServerSocketChannel chnl, Exception e)
            {
                System.out.println(chnl+": onListenFail");
                e.printStackTrace();
            }
            @Override
            public void onConnect(SocketChannel chnl)
            {
                System.out.println(chnl+": onConnect");
                st1.sendMessage(chnl,new Packet().pushData("hey there! :)".getBytes()));
            }
            @Override
            public void onAcceptFail(ServerSocketChannel chnl, Exception e)
            {
                System.out.println(chnl+": onAcceptFail");
                e.printStackTrace();
            }
            @Override
            public void onConnectFail(SocketChannel chnl, Exception e)
            {
                System.out.println(chnl+": onConnectFail");
                e.printStackTrace();
            }
            @Override
            public void onMessage(SocketChannel chnl, Packet packet)
            {
                System.out.println(chnl+": "+new String(packet.popData()));
                st2.disconnect(chnl);
            }
            @Override
            public void onClose(SocketChannel chnl, boolean remote)
            {
                System.out.println(chnl+": onClose");
            }
        };

        st2.startListening(7000);
        st1.connect("localhost",7000);

        while(true)
        {
        st1.handleMessages(listener);
        st2.handleMessages(listener);
        }
    }
}
