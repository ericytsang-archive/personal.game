package game;

import java.util.LinkedHashSet;
import java.util.Set;

import framework.GameLoop;
import framework.net.Entity;
import framework.net.Mux;
import framework.net.ServerController;
import net.Host;
import net.Packet;

public class ServerMux<ClientKey> extends Mux<ClientKey>
{
    private int nextEntityId;

    private GameLoop gameLoop;

    /**
     * set of entities shared across clients
     */
    private Set<framework.net.Entity> sharedEntities;

    public ServerMux(Host<ClientKey> adaptee, GameLoop gameLoop)
    {
        super(adaptee);
        this.gameLoop = gameLoop;
        this.nextEntityId = 0;
        this.sharedEntities = new LinkedHashSet<>();
    }

    @Override
    public Entity makeEntity(int id, PairType pairType, Packet packet)
    {
        Entity ret;

        switch(pairType)
        {
        case SVRCMD_CLNTCMD:
            System.out.println("Command Entity Created");
            ret = new ServerCommand(id,pairType);
            break;
        case CMDCTRL_NETCTRL:
            ret = new ServerController(id,pairType);
            break;
        default:
            throw new RuntimeException("default case hit");
        }

        return ret;
    }

    public Entity makeEntity(PairType pairType, Packet packet)
    {
        return makeEntity(nextEntityId++,pairType,packet);
    }

    /**
     * invoked when when a connection to a new client is established.
     *
     * sets up the initial entities used to communicate with the new client.
     *
     * registers existing shared network entities with the newly connected
     *   client.
     *
     * registers special network entities with the newly connected clients.
     *
     * adds new shared entities to the set of shared entities, and registers it
     *   with all previously connected clients.
     */
    @Override
    public void onOpen(ClientKey conn)
    {
        super.onOpen(conn);

        // create the command and controller entities on the server side, and
        // link them so that commands received on the command entity get piped
        // to the controller
        ServerCommand cmd = (ServerCommand) makeEntity(PairType.SVRCMD_CLNTCMD,null);
        ServerController ctrl = (ServerController) makeEntity(PairType.CMDCTRL_NETCTRL,null);
        Gunner gunner = new Gunner(ctrl,60,60);
        ctrl.setControllee(gunner);
        gunner.setGameLoop(gameLoop);
        cmd.setServerController(ctrl);
        register(conn,cmd,new Packet());

        // register all shared entities with the new client
        for(framework.net.Entity e : sharedEntities)
        {
            register(conn,e,e.getRegisterPacket());
        }

        // register the new shared entity with all currently connected clients
        registerWithAll(ctrl,ctrl.getRegisterPacket());

        // add the new shared entity created to the set of shared entities to be
        // added to future clients that connect
        sharedEntities.add(ctrl);
    }

    @Override
    public void onError(Object obj, Exception e)
    {
        throw new RuntimeException(e);
    }
}
