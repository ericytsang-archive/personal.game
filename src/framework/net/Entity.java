package framework.net;

import game.PairType;
import net.Packet;

public abstract class Entity
{
    private final int id;
    private final PairType pairType;
    public Entity(int id, PairType pairType)
    {
        this.id = id;
        this.pairType = pairType;
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
    public final void update(Packet packet)
    {
        Mux.getInstance().sendMessageToAll(this,MuxMsg.UPDATE,packet);
    }
    public final void unregister(Packet packet)
    {
        Mux.getInstance().sendMessageToAll(this,MuxMsg.UNREGISTER,packet);
    }
}
