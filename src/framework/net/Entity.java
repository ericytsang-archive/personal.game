package framework.net;

import java.util.Set;
import java.util.LinkedHashSet;

import game.PairType;
import net.Packet;

public abstract class Entity
{
    private final int id;
    private static int nextId = 0;
    private final PairType pairType;
    final Set<Object> registeredClients;
    public Entity(int id, PairType pairType)
    {
        this.id = id;
        this.pairType = pairType;
        this.registeredClients = new LinkedHashSet<>();
    }
    public Entity(PairType pairType)
    {
        this(nextId++,pairType);
    }
    public final int getId()
    {
        return id;
    }
    public final PairType getPairType()
    {
        return pairType;
    }
    public abstract Packet getRegisterPacket();
    public abstract void onUpdate(Packet packet);
    public abstract void onUnregister(Packet packet);
    public final void register(Object client, Packet packet)
    {
        Mux.getInstance().register(client,this,packet);
    }
    public final void update(Packet packet)
    {
        Mux.getInstance().update(this,packet);
    }
    public final void unregister(Object client, Packet packet)
    {
        Mux.getInstance().unregister(client,this,packet);
    }
}
