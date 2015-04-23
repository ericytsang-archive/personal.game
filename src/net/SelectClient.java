package net;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public abstract class SelectClient implements Client<SocketChannel>, SelectThread.SelectListener
{
    /**
     * the select thread of this class. isn't instantiated directly, instead,
     *   get the instane's select thread using the getSelectThread method.
     */
    private SelectThread selectThread;

    private ClientListener<SocketChannel> observer;

    private static final NullClientListener<SocketChannel> NULL_OBSERVER
            = new NullClientListener<>();

    /////////////////
    // constructor //
    /////////////////

    public SelectClient()
    {
        this.observer = NULL_OBSERVER;
    }

    //////////////////////
    // public interface //
    //////////////////////

    public SelectClient setObserver(ClientListener<SocketChannel> observer)
    {
        this.observer = (observer != null) ? observer : NULL_OBSERVER;
        return this;
    }

    //////////////////////////////////////////////
    // public interface & Client implementation //
    //////////////////////////////////////////////

    @Override
    public SocketChannel connect(String remoteName, int remotePort)
    {
        return getSelectThread().connect(remoteName,remotePort);
    }

    @Override
    public void disconnect(SocketChannel channel)
    {
        getSelectThread().disconnect(channel);
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
    public final void onConnect(SocketChannel conn)
    {
        observer.onConnect(conn);
    }

    @Override
    public final void onConnectFail(SocketChannel conn, Exception e)
    {
        observer.onConnectFail(conn,e);
    }

    @Override
    public final void onMessage(SocketChannel conn, Packet packet)
    {
        observer.onMessage(conn,packet);
    }

    @Override
    public final void onClose(SocketChannel conn, boolean remote)
    {
        observer.onClose(conn,remote);
    }

    // unused callbacks

    @Override
    public final void onAccept(SocketChannel chnl)
    {
        throw new UnsupportedOperationException("method is an unused callback");
    }

    @Override
    public final void onAcceptFail(ServerSocketChannel chnl, Exception e)
    {
        throw new UnsupportedOperationException("method is an unused callback");
    }

    @Override
    public final void onListenFail(ServerSocketChannel chnl, Exception e)
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
