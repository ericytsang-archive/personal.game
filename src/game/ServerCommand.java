package game;

import framework.net.ServerController;
import net.Packet;

public class ServerCommand extends framework.net.Entity
{
    private ServerController svrCtrl;

    //////////////////
    // constructors //
    //////////////////

    public ServerCommand()
    {
        super(PairType.SVRCMD_CLNTCMD);
    }

    //////////////////////
    // public interface //
    //////////////////////

    public void setServerController(ServerController svrCtrl)
    {
        this.svrCtrl = svrCtrl;
    }

    //////////////////////////
    // framework.net.Entity //
    //////////////////////////

    @Override
    public Packet getRegisterPacket()
    {
        return new Packet();
    }

    @Override
    public void onUpdate(Packet packet)
    {
        if(svrCtrl != null)
        {
            svrCtrl.addEvent(packet);
        }
    }

    @Override
    public void onUnregister(Packet packet)
    {
        // do nothing
    }
}
