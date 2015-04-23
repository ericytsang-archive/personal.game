package game;

import javax.swing.JFrame;

import framework.Canvas;
import framework.GameLoop;
import framework.net.ClientController;
import framework.net.Entity;
import framework.net.Mux;
import net.Host;
import net.Packet;

public class ClientMux<ClientKey> extends Mux<ClientKey>
{
    private JFrame frame;

    private Canvas canvas;

    private GameLoop gameLoop;

    public ClientMux(Host<ClientKey> adaptee, JFrame frame, Canvas canvas, GameLoop gameLoop)
    {
        super(adaptee);
        this.frame = frame;
        this.canvas = canvas;
        this.gameLoop = gameLoop;
    }

    @Override
    public Entity makeEntity(int id, PairType pairType, Packet packet)
    {
        Entity ret;

        switch(pairType)
        {
        case SVRCMD_CLNTCMD:
            System.out.println("Command Entity Created");
            ClientCommand cmd = new ClientCommand(id,pairType);
            frame.addKeyListener(cmd);
            frame.addMouseListener(cmd);
            ret = cmd;
            break;
        case CMDCTRL_NETCTRL:
            ClientController ctrl = new ClientController(id,pairType);
            String controlleeName = new String(packet.popData());
            if(controlleeName.equals(Gunner.class.getSimpleName()))
            {
                Gunner gunner = new Gunner(ctrl,0,0).fromBytes(packet.popData());
                gunner.setCanvas(canvas);
                gunner.setGameLoop(gameLoop);
            }
            ret = ctrl;
            break;
        default:
            throw new RuntimeException("default case hit");
        }

        return ret;
    }

    @Override
    public void onError(Object obj, Exception e)
    {
        throw new RuntimeException(e);
    }
}
