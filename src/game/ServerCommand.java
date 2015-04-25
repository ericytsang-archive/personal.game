package game;

import java.awt.Color;
import java.nio.ByteBuffer;

import framework.GameLoop;
import framework.net.ServerController;
import framework.net.Mux;
import net.Packet;

public class ServerCommand extends framework.net.Entity
{
    private ServerController svrCtrl;

    private GameLoop gameLoop;

    //////////////////
    // constructors //
    //////////////////

    public ServerCommand(GameLoop gameLoop)
    {
        super(PairType.SVRCMD_CLNTCMD);
        this.gameLoop = gameLoop;
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
        ByteBuffer buf = ByteBuffer.wrap(packet.peekData());
        switch(Command.values()[buf.getInt()])
        {
        case MAKE_BULLET:
            ServerController ctrl = new ServerController();
            Bullet bullet = new Bullet(ctrl,0,0,5,5,Color.RED);
            ctrl.setControllee(bullet);
            bullet.setGameLoop(gameLoop);
            Mux.getInstance().registerWithAll(ctrl,ctrl.getRegisterPacket());
            break;
        case JUMP:
        case MOVE_D:
        case MOVE_L:
        case MOVE_R:
        case MOVE_U:
            if(svrCtrl != null)
            {
                svrCtrl.addEvent(packet);
            }
            break;
        default:
            throw new RuntimeException("default case hit");
        }
    }

    @Override
    public void onUnregister(Packet packet)
    {
        // do nothing
    }
}
