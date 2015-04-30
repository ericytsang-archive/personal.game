package game;

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
            Gunner gunner = (Gunner) svrCtrl.getControllee();

            // parse packet information
            int targetX = buf.getInt();
            int targetY = buf.getInt();
            float angle = getAngle(gunner.getX(),gunner.getY(),targetX,targetY);

            // create the bullet, and the controller for the bullet
            ServerController ctrl = new ServerController();
            Bullet bullet = new Bullet(ctrl,gunner.getX(),gunner.getY(),angle,gunner.getRenderColor());

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

    private float getAngle(int x1, int y1, int x2, int y2)
    {
        return (float) Math.atan2(y2-y1,x2-x1);
    }
}
