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
    private GameLoop gameLoop;

    /**
     * set of entities shared across clients
     */
    private Set<framework.net.Entity> sharedEntities;

    public ServerMux(Host<ClientKey> adaptee, GameLoop gameLoop)
    {
        super(adaptee);
        this.sharedEntities = new LinkedHashSet<>();
        this.gameLoop = gameLoop;
    }

    @Override
    protected Entity onRegister(int id, PairType pairType, Packet packet)
    {
        throw new RuntimeException("onRegister called on server");
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
        ServerCommand cmd = new ServerCommand();
        ServerController ctrl = new ServerController();
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
}
