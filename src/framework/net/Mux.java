package framework.net;

import framework.Serializable;
import game.PairType;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.Host;
import net.HostListener;
import net.Packet;

public abstract class Mux<ClientKey> implements HostListener<ClientKey>
{
    private static Mux<?> instance;

    private final Host<ClientKey> adaptee;

    private final Set<ClientKey> clients;

    private final Map<Integer,framework.net.Entity> entities;

    //////////////////
    // constructors //
    //////////////////

    public Mux(Host<ClientKey> adaptee)
    {
        this.adaptee = adaptee;
        this.clients = new LinkedHashSet<>();
        this.entities = new LinkedHashMap<>();
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

    public final void register(ClientKey client, Entity entity, Packet packet)
    {
        entities.put(entity.getId(),entity);
        entity.registeredClients.add(client);
        sendMuxMsg(client,entity.getId(),entity.getPairType(),MuxMsg.REGISTER,packet);
    }

    public final void registerWithAll(Entity entity, Packet packet)
    {
        entities.put(entity.getId(),entity);
        for(ClientKey c : clients)
        {
            entity.registeredClients.add(c);
            sendMuxMsg(c,entity.getId(),entity.getPairType(),MuxMsg.REGISTER,packet);
        }
    }

    public final void unregisterWithAll(Entity entity, Packet packet)
    {
        entities.put(entity.getId(),entity);
        for(ClientKey c : clients)
        {
            entity.registeredClients.add(c);
            sendMuxMsg(c,entity.getId(),entity.getPairType(),MuxMsg.UNREGISTER,packet);
        }
    }

    public final void unregister(ClientKey client, Entity entity, Packet packet)
    {
        entities.remove(entity.getId());
        entity.registeredClients.remove(client);
        sendMuxMsg(client,entity.getId(),entity.getPairType(),MuxMsg.UNREGISTER,packet);
    }

    /////////////////////////
    // protected interface //
    /////////////////////////

    @SuppressWarnings("unchecked")
    protected final void update(Entity entity, Packet packet)
    {
        sendMuxMsgToGroup((Set<ClientKey>) entity.registeredClients,entity.getId(),entity.getPairType(),MuxMsg.UPDATE,packet);
    }

    protected abstract Entity onRegister(int id, PairType pairType, Packet packet);

    ///////////////////////
    // private interface //
    ///////////////////////

    private void onMessage(ClientKey conn, int id, PairType pairType, MuxMsg msgType, Packet packet)
    {
        switch(msgType)
        {
        case REGISTER:
            entities.put(id,onRegister(id,pairType,packet));
            entities.get(id).registeredClients.add(conn);
            break;
        case UPDATE:
            entities.get(id).onUpdate(packet);
            break;
        case UNREGISTER:
            entities.get(id).onUnregister(packet);
            entities.get(id).registeredClients.remove(conn);
            entities.remove(id);
            break;
        default:
            throw new RuntimeException("default case hit");
        }
    }

    private void sendMuxMsgToGroup(Set<ClientKey> clients, int id, PairType pairType, MuxMsg msgType, Packet packet)
    {
        // prepare the packet with custom header data
        packet = packet.pushData(new MuxHeader(id,pairType,msgType).toBytes());

        // send the packet to all connected clients
        sendMessageToGroup(clients,packet);
    }

    private void sendMuxMsg(ClientKey client, int id, PairType pairType, MuxMsg msgType, Packet packet)
    {
        // prepare the packet with custom header data
        packet = packet.pushData(new MuxHeader(id,pairType,msgType).toBytes());

        // send the packet to all connected clients
        sendMessage(client,packet);
    }

    private void sendMessageToGroup(Set<ClientKey> clients, Packet packet)
    {
        // send the packet to all connected clients
        for(ClientKey c : clients)
        {
            adaptee.sendMessage(c,packet);
        }
    }

    private void sendMessage(ClientKey client, Packet packet)
    {
        adaptee.sendMessage(client,packet);
    }

    /////////////////////////////
    // HostListener<ClientKey> //
    /////////////////////////////

    public void onOpen(ClientKey conn)
    {
        System.out.println("connection"+conn.hashCode()+" opened");
        clients.add(conn);
    }

    public void onMessage(ClientKey conn, Packet packet)
    {
        // parse header data from packet
        MuxHeader hdr = new MuxHeader(packet.peekData());
        packet = packet.popData();

        // invoke callback
        onMessage(conn,hdr.id,hdr.pairType,hdr.msgType,packet);
    }

    @Override
    public void onError(Object obj, Exception e)
    {
        e.printStackTrace();
    }

    @Override
    public void onClose(ClientKey conn, boolean remote)
    {
        System.out.println("connection"+conn.hashCode()+" closed by "+(remote?"remote":"local")+" host");
        clients.remove(conn);
    }

    ///////////////
    // MuxHeader //
    ///////////////

    private class MuxHeader implements Serializable
    {
        public int id;
        public PairType pairType;
        public MuxMsg msgType;
        public MuxHeader(int id, PairType pairType, MuxMsg msgType)
        {
            this.id = id;
            this.pairType = pairType;
            this.msgType = msgType;
        }
        public MuxHeader(byte[] data)
        {
            fromBytes(data);
        }
        @Override
        public MuxHeader fromBytes(byte[] data)
        {
            // parse header data from packet
            ByteBuffer buf = ByteBuffer.wrap(data);
            id = buf.getInt();
            pairType = PairType.values()[buf.getInt()];
            msgType = MuxMsg.values()[buf.getInt()];
            return this;
        }
        @Override
        public byte[] toBytes()
        {
            ByteBuffer buf = ByteBuffer.allocate(12);
            buf.putInt(id);
            buf.putInt(pairType.ordinal());
            buf.putInt(msgType.ordinal());
            return buf.array();
        }
    }
}
