package framework;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import framework.net.GameClient;
import framework.net.Mux;
import game.ClientMux;

import javax.swing.JFrame;

import net.HostAdapter;
import net.HostListenerAdapter;

public class ClientMain
{
    public static final int FRAME_WIDTH = 500;

    public static final int FRAME_HEIGHT = 500;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Awesome Game");
        Canvas canvas = new Canvas();
        GameLoop gameLoop = new GameLoop(canvas);
        GameClient clnt = new GameClient();
        Mux.setInstance(new ClientMux<SocketChannel>(
                new HostAdapter<SocketChannel,ServerSocketChannel>(clnt),
                frame,canvas,gameLoop));

        frame.setContentPane(canvas);
        frame.setSize(FRAME_WIDTH,FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        clnt.setObserver(
                new HostListenerAdapter<SocketChannel,ServerSocketChannel>()
                .setObserver(Mux.<SocketChannel>getInstance()));
        clnt.connect(args[0],7000);

        gameLoop.register(clnt);
        gameLoop.loop();
    }
}
