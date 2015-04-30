package framework.net;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import net.Packet;
import framework.Controller;
import framework.GameEntity;
import game.PairType;

public class ServerController extends framework.net.Entity implements Controller
{
    private Queue<Packet> events;

    private GameEntity controllee;

    public ServerController()
    {
        super(PairType.SVRCTRL_NETCTRL);
        this.events = new LinkedBlockingQueue<>();
    }

    public void setControllee(GameEntity controllee)
    {
        this.controllee = controllee;
    }

    public framework.Entity getControllee()
    {
        return controllee;
    }

    public void addEvent(Packet packet)
    {
        events.add(packet);
        update(packet);
    }

    @Override
    public final Packet[] getEvents()
    {
        controllee.serverUpdate();
        Packet[] eventsArr = new Packet[events.size()];
        events.toArray(eventsArr);
        events.clear();
        return eventsArr;
    }

    @Override
    public final void onUpdate(Packet packet)
    {
        events.add(packet);
    }

    @Override
    public Packet getRegisterPacket()
    {
        return new Packet()
            .pushData(controllee.toBytes())
            .pushData(controllee.getClass().getSimpleName().getBytes());
    }

    @Override
    public void onUnregister(Packet packet)
    {
        controllee.unsetCanvas();
        controllee.unsetGameLoop();
    }
}
