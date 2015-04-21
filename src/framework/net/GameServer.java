package framework.net;

import framework.InputEntity;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.HostListener;
import net.NullHostListener;
import net.Packet;
import net.SelectServer;

public class GameServer extends SelectServer implements InputEntity
{
    private HostListener<SocketChannel> listener = new NullHostListener<>();

    public GameServer()
    {
        super();
    }

    //////////////////////
    // public interface //
    //////////////////////

    public void setHostListener(HostListener<SocketChannel> listener)
    {
        this.listener = (listener == null) ?
                new NullHostListener<SocketChannel>() : listener;
    }

    ///////////////////
    // InputProvider //
    ///////////////////

    @Override
    public void processInputs()
    {
        handleMessages(this);
    }

    //////////////////
    // SelectServer //
    //////////////////

    @Override
    public void onAcceptFail(ServerSocketChannel channel, Exception e)
    {
        System.out.println("onAcceptFail: "+e);
    }

    @Override
    public void onListenFail(ServerSocketChannel channel, Exception e)
    {
        System.out.println("onListenFail: "+e);
    }

    @Override
    public void onAccept(SocketChannel channel)
    {
        System.out.println("onAccept: "+channel);
    }

    @Override
    public void onOpen(SocketChannel channel)
    {
        listener.onOpen(channel);
    }

    @Override
    public void onMessage(SocketChannel conn, Packet packet)
    {
        listener.onMessage(conn,packet);
    }

    @Override
    public void onClose(SocketChannel channel, boolean remote)
    {
        listener.onClose(channel,remote);
    }
}
