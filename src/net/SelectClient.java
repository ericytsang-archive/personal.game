package net;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.SelectThread.SelectListsner;

public abstract class SelectClient implements Client<SocketChannel>, SelectThread.SelectListsner
{
    /**
     * the select thread of this class. isn't instantiated directly, instead,
     *   get the instane's select thread using the getSelectThread method.
     */
    private SelectThread selectThread;

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

    public void handleMessages(SelectListsner listener)
    {
        getSelectThread().handleMessages(this);
    }

    /////////////////////////////////
    // SelectThread.SelectListsner //
    /////////////////////////////////

    // callbacks

    @Override
    public abstract void onConnect(SocketChannel conn);

    @Override
    public abstract void onConnectFail(SocketChannel conn, Exception e);

    @Override
    public abstract void onMessage(SocketChannel conn, Packet packet);

    @Override
    public abstract void onClose(SocketChannel conn, boolean remote);

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
        }
        return selectThread;
    }
}
