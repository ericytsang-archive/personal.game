package framework.net;

import game.PairType;

import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.Host;
import net.HostListener;
import net.Packet;

public abstract class Mux<ClientKey> implements HostListener<ClientKey>
{
    private static Mux<?> instance;

    private Map<Integer,Entity> entities;

    private Host<ClientKey> adaptee;

    private Set<ClientKey> clients;

    //////////////////
    // constructors //
    //////////////////

    public Mux(Host<ClientKey> adaptee)
    {
        this.entities = new LinkedHashMap<>();
        this.clients = new LinkedHashSet<>();
        this.adaptee = adaptee;
    }

    //////////////////////
    // public interface //
    //////////////////////

    public static void setInstance(Mux<?> mux)
    {
        if(instance == null)
        {
            instance = mux;
        }
        else
        {
            throw new RuntimeException("instance already set.");
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> Mux<T> getInstance()
    {
        if(instance == null)
        {
            throw new RuntimeException("instance not yet set. need to call setInstance first");
        }
        else
        {
            return (Mux<T>) instance;
        }
    }

    public final void sendMessageToAll(Entity entity, MuxMsg msgType, Packet packet)
    {
        sendMessageToAll(entity.getId(),entity.getPairType(),msgType,packet);
    }

    public final void sendMessageToAll(int id, PairType pairType, MuxMsg msgType, Packet packet)
    {
        // send message to self
        onMessage(id,pairType.ordinal(),msgType,packet);

        // prepare the packet with custom header data
        ByteBuffer buf = ByteBuffer.allocate(12);
        buf.putInt(id);
        buf.putInt(pairType.ordinal());
        buf.putInt(msgType.ordinal());
        packet.pushData(buf.array());

        // send the packet to all connected clients
        for(ClientKey c : clients)
        {
            adaptee.sendMessage(c,packet);
        }
    }

    public final void sendMessage(ClientKey client, Entity entity, MuxMsg msgType, Packet packet)
    {
        // send message to self
        sendMessage(client,entity.getId(),entity.getPairType(),msgType,packet);
    }

    public final void sendMessage(ClientKey client, int id, PairType pairType, MuxMsg msgType, Packet packet)
    {
        // send message to self
        onMessage(id,pairType.ordinal(),msgType,packet);

        // prepare the packet with custom header data
        ByteBuffer buf = ByteBuffer.allocate(12);
        buf.putInt(id);
        buf.putInt(pairType.ordinal());
        buf.putInt(msgType.ordinal());
        packet.pushData(buf.array());

        // send the packet to specified client
        adaptee.sendMessage(client,packet);
    }

    public final void registerWithAll(Entity entity, Packet packet)
    {
        // register with self
        entities.put(entity.getId(),entity);

        // prepare the packet with custom header data
        ByteBuffer buf = ByteBuffer.allocate(12);
        buf.putInt(entity.getId());
        buf.putInt(entity.getPairType().ordinal());
        buf.putInt(MuxMsg.REGISTER.ordinal());
        packet.pushData(buf.array());

        // send the packet to all connected clients
        for(ClientKey c : clients)
        {
            adaptee.sendMessage(c,packet);
        }
    }

    public final void register(ClientKey client, Entity entity, Packet packet)
    {
        // register with self
        entities.put(entity.getId(),entity);

        // prepare the packet with custom header data
        ByteBuffer buf = ByteBuffer.allocate(12);
        buf.putInt(entity.getId());
        buf.putInt(entity.getPairType().ordinal());
        buf.putInt(MuxMsg.REGISTER.ordinal());
        packet.pushData(buf.array());

        // send the packet to specified client
        adaptee.sendMessage(client,packet);
    }

    protected abstract Entity makeEntity(int id, PairType pairType, Packet packet);

    private void onMessage(int id, int pairType, MuxMsg msgType, Packet packet)
    {
        switch(msgType)
        {
        case REGISTER:
            entities.put(id,makeEntity(id,PairType.values()[pairType],packet));
            break;
        case UNREGISTER:
            entities.get(id).onUnregister(packet);
            entities.remove(id);
            break;
        case UPDATE:
            entities.get(id).onUpdate(packet);
            break;
        default:
            throw new RuntimeException("default case hit");
        }
    }

    ////////////////////
    // HostListener<> //
    ////////////////////

    @Override
    public void onOpen(ClientKey conn)
    {
        System.out.println("connection"+conn.hashCode()+" opened");
        clients.add(conn);
    }

    @Override
    public void onMessage(ClientKey conn, Packet packet)
    {
        // parse header data from packet
        ByteBuffer buf = ByteBuffer.wrap(packet.popData());
        int id = buf.getInt();
        int pairType = buf.getInt();
        MuxMsg msgType = MuxMsg.values()[buf.getInt()];

        // invoke callback
        onMessage(id,pairType,msgType,packet);
    }

    @Override
    public void onClose(ClientKey conn, boolean remote)
    {
        System.out.println("connection"+conn.hashCode()+" closed by "+(remote?"remote":"local")+" host");
        clients.remove(conn);
    }
}
