package framework;

import java.nio.channels.SocketChannel;

import framework.net.GameServer;
import framework.net.Mux;
import game.ServerMux;

public class ServerMain
{
    public static void main(String[] args)
    {
        GameLoop gameLoop = new GameLoop();
        GameServer svr = new GameServer();
        Mux.setInstance(new ServerMux<SocketChannel>(svr,gameLoop));
        svr.setHostListener(Mux.<SocketChannel>getInstance());
        svr.startListening(7000);
        gameLoop.register(svr);
        gameLoop.loop();
    }
}
