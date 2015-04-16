package game;

import java.awt.Color;

import javax.swing.JFrame;

public class Main
{
    public static final int FRAME_WIDTH = 500;

    public static final int FRAME_HEIGHT = 500;

    public static void main(String[] args) throws InterruptedException
    {
        JFrame frame = new JFrame("Awesome Game");
        Canvas canvas = new Canvas();
        GameLoop gameLoop = new GameLoop(canvas);

        frame.setContentPane(canvas);
        frame.setSize(FRAME_WIDTH,FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        canvas.setBackground(Color.CYAN);

        new GameEntity(gameLoop,canvas);

        gameLoop.loop();
    }
}
