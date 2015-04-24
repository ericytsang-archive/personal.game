package framework.net;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import net.Packet;
import framework.Controller;
import game.PairType;

public class ClientController extends framework.net.Entity implements Controller
{
    private Queue<Packet> events;

    public ClientController(int id)
    {
        super(id,PairType.SVRCTRL_NETCTRL);
        this.events = new LinkedBlockingQueue<>();
    }

    @Override
    public final Packet[] getEvents()
    {
        Packet[] eventsArr = new Packet[events.size()];
        events.toArray(eventsArr);
        events.clear();
        return eventsArr;
    }

    @Override
    public final void onUpdate(Packet packet)
    {
        System.out.println("new event");
        events.add(packet);
    }

    @Override
    public Packet getRegisterPacket()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onUnregister(Packet packet)
    {
        // TODO Auto-generated method stub
        
    }
}
