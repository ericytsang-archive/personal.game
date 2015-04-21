package framework.net;

import framework.InputEntity;

import java.nio.channels.SocketChannel;

import net.Host;
import net.HostListener;
import net.NullHostListener;
import net.Packet;
import net.SelectClient;

public class GameClient extends SelectClient implements InputEntity, Host<SocketChannel>
{
    private SocketChannel channel;

    private HostListener<SocketChannel> listener = new NullHostListener<>();

    public GameClient()
    {
        super();
        this.channel = null;
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
    // SelectClient //
    //////////////////

    @Override
    public void sendMessage(SocketChannel channel, Packet packet)
    {
        super.sendMessage(channel,packet);
    }

    @Override
    public void onConnect(SocketChannel conn)
    {
        if(channel == null)
        {
            channel = conn;
        }
        else
        {
            disconnect(conn);
        }
    }

    @Override
    public void onConnectFail(SocketChannel conn, Exception e)
    {
        System.out.println("failed to connect to remote host: "+e);
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
    public void onClose(SocketChannel conn, boolean remote)
    {
        listener.onClose(conn,remote);
    }
}
