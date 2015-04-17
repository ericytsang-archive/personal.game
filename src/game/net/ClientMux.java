package game.net;

import game.InputProvider;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;

import net.Packet;
import net.SelectClient;

public class ClientMux extends SelectClient implements InputProvider
{
    private Map<Integer,Entity> entities;

    private SocketChannel channel;

    public ClientMux()
    {
        super();
        this.channel = null;
        this.entities = new LinkedHashMap<>();
    }

    public void sendMessage(Entity entity, MuxMsg msgType, Packet packet)
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
    // SelectClient //
    //////////////////

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
    public void onMessage(SocketChannel conn, Packet packet)
    {
        ByteBuffer buf = ByteBuffer.wrap(packet.popData());
        int id = buf.getInt();
        int pairType = buf.getInt();
        MuxMsg msgType = MuxMsg.values()[buf.getInt()];
        onMessage(id,pairType,msgType,packet);
    }

    @Override
    public void onClose(SocketChannel conn, boolean remote)
    {
        System.out.println("connection"+conn.hashCode()+" closed");
    }
}
