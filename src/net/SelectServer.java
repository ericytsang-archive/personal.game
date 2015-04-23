package net;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public abstract class SelectServer implements Server<SocketChannel,ServerSocketChannel>, SelectThread.SelectListener
{
    /**
     * the select thread of this class. isn't instantiated directly, instead,
     *   get the instane's select thread using the getSelectThread method.
     */
    private SelectThread selectThread;

    private ServerListener<SocketChannel,ServerSocketChannel> observer;

    private static final ServerListener<SocketChannel,ServerSocketChannel>
            NULL_OBSERVER = new NullServerListener<>();

    /////////////////
    // constructor //
    /////////////////

    public SelectServer()
    {
        this.observer = NULL_OBSERVER;
    }

    //////////////////////
    // public interface //
    //////////////////////

    public SelectServer setObserver(ServerListener<SocketChannel,ServerSocketChannel> observer)
    {
        this.observer = (observer != null) ? observer : NULL_OBSERVER;
        return this;
    }

    //////////////////////////////////////////////
    // public interface & Server implementation //
    //////////////////////////////////////////////

    @Override
    public ServerSocketChannel startListening(int serverPort)
    {
        return getSelectThread().startListening(serverPort);
    }

    @Override
    public void stopListening(ServerSocketChannel channel)
    {
        getSelectThread().stopListening(channel);
    }

    @Override
    public void sendMessage(SocketChannel channel, Packet packet)
    {
        getSelectThread().sendMessage(channel,packet);
    }

    public void sendMessageOnThisThread(SocketChannel channel, Packet packet)
    {
        getSelectThread().sendMessageOnThisThread(channel,packet);
    }

    public void handleMessages(SelectThread.SelectListener listener)
    {
        getSelectThread().handleMessages(this);
    }

    /////////////////////////////////
    // SelectThread.SelectListsner //
    /////////////////////////////////

    // callbacks

    @Override
    public final void onAcceptFail(ServerSocketChannel channel, Exception e)
    {
        observer.onAcceptFail(channel,e);
    }

    @Override
    public final void onListenFail(ServerSocketChannel channel, Exception e)
    {
        observer.onListenFail(channel,e);
    }

    @Override
    public final void onAccept(SocketChannel channel)
    {
        observer.onAccept(channel);
    }

    @Override
    public final void onMessage(SocketChannel channel, Packet packet)
    {
        observer.onMessage(channel,packet);
    }

    @Override
    public final void onClose(SocketChannel channel, boolean remote)
    {
        observer.onClose(channel,remote);
    }

    // unused callbacks

    @Override
    public final void onConnect(SocketChannel chnl)
    {
        throw new UnsupportedOperationException("method is an unused callback");
    }

    @Override
    public final void onConnectFail(SocketChannel chnl, Exception e)
    {
        throw new UnsupportedOperationException("method is an unused callback");
    }

    ///////////////////////
    // private interface //
    ///////////////////////

    private SelectThread getSelectThread()
    {
        if(selectThread == null)
        {
            selectThread = new SelectThread();
            selectThread.start();
        }
        return selectThread;
    }
}
