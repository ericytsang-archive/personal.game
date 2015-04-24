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
    private final JFrame frame;

    private final Canvas canvas;

    private final GameLoop gameLoop;

    public ClientMux(Host<ClientKey> adaptee, JFrame frame, Canvas canvas, GameLoop gameLoop)
    {
        super(adaptee);
        this.frame = frame;
        this.canvas = canvas;
        this.gameLoop = gameLoop;
    }

    @Override
    protected Entity onRegister(int id, PairType pairType, Packet packet)
    {
        Entity ret;

        switch(pairType)
        {
        case SVRCMD_CLNTCMD:
            ClientCommand cmd = new ClientCommand(id);
            frame.addKeyListener(cmd);
            frame.addMouseListener(cmd);
            ret = cmd;
            break;
        case SVRCTRL_NETCTRL:
            ClientController ctrl = new ClientController(id);
            String controlleeName = new String(packet.peekData());
            packet = packet.popData();
            if(controlleeName.equals(Gunner.class.getSimpleName()))
            {
                System.out.println("Making gunner...");
                Gunner gunner = new Gunner(ctrl,0,0).fromBytes(packet.peekData());
                packet = packet.popData();
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
}
