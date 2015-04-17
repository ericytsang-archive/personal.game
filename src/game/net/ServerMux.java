package game.net;

import game.InputProvider;

import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import net.Packet;
import net.SelectServer;

public class ServerMux extends SelectServer implements InputProvider
{
    private Map<Integer,Entity> entities;

    public void sendMessage(SocketChannel channel, Entity entity, MuxMsg msgType, Packet packet)
    {
        ByteBuffer buf = ByteBuffer.allocate(12);
        buf.putInt(entity.getId());
        buf.putInt(entity.getPairType());
        buf.putInt(msgType.ordinal());
        packet.pushData(buf.array());
        sendMessage(channel,packet);
    }

    public void onMessage(int id, int pairType, MuxMsg msgType, Packet packet)
    {
        switch(msgType)
        {
        case REGISTER:
            break;
        case UNREGSTER:
            entities.get(id).onUnregister(packet);
            break;
        case UPDATE:
            entities.get(id).onUpdate(packet);
            break;
        default:
            throw new RuntimeException("default case hit");
        }
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
        // create a new player, and associate it with the connection
        
    }

    @Override
    public void onMessage(SocketChannel conn, Packet packet)
    {
        ByteBuffer buf = ByteBuffer.wrap(packet.popData());
        int id = buf.getInt();
        int pairType = buf.getInt();
        MuxMsg msgType = MuxMsg.values()[buf.getInt()];
        onMessage(id,pairType,msgType,packet);
    }

    @Override
    public void onClose(SocketChannel channel, boolean remote)
    {
        // kill the player associated with the disconnected player
    }
}
