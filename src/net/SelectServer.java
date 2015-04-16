package net;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.SelectThread.SelectListsner;

public abstract class SelectServer implements Server<ServerSocketChannel,SocketChannel>, SelectThread.SelectListsner
{
    /**
     * the select thread of this class. isn't instantiated directly, instead,
     *   get the instane's select thread using the getSelectThread method.
     */
    private SelectThread selectThread;

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

    public void handleMessages(SelectListsner listener)
    {
        getSelectThread().handleMessages(this);
    }

    /////////////////////////////////
    // SelectThread.SelectListsner //
    /////////////////////////////////

    // callbacks

    @Override
    public abstract void onAcceptFail(ServerSocketChannel channel, Exception e);

    @Override
    public abstract void onListenFail(ServerSocketChannel channel, Exception e);

    @Override
    public abstract void onAccept(SocketChannel channel);

    @Override
    public abstract void onMessage(SocketChannel channel, Packet packet);

    @Override
    public abstract void onClose(SocketChannel channel, boolean remote);

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
        }
        return selectThread;
    }
}
