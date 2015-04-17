package game.net;

import net.Packet;

public interface Entity
{
    public int getId();
    public int getPairType();
    public void onUpdate(Packet packet);
    public void update(Packet packet);
    public void onUnregister(Packet packet);
    public void unregister(Packet packet);
}
