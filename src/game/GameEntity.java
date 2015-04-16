package game;

import java.awt.Color;
import java.awt.Graphics;

public class GameEntity implements Entity, Drawable
{
    private GameLoop gameLoop;
    private Canvas canvas;
    private int xSpeed;
    private int ySpeed;
    private int x;
    private int y;

    public GameEntity(GameLoop gameLoop, Canvas canvas)
    {
        setGameLoop(gameLoop);
        setCanvas(canvas);
        x = 5;
        y = 5;
        xSpeed = 1;
        ySpeed = 0;
    }

    public void update()
    {
        x += xSpeed;
        y += ySpeed;
    }

    public void render(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.drawArc(x,y,10,10,0,360);
    }

    public int getRenderDepth()
    {
        return 0;
    }

    ///////////////////////
    // private interface //
    ///////////////////////

    private void setCanvas(Canvas newCanvas)
    {
        // unregister from previous canvas
        unsetCanvas();

        newCanvas.register(this);
        canvas = newCanvas;
    }

    private void unsetCanvas()
    {
        // if we are currently registered with a game loop, unregister from it
        if(canvas != null)
        {
            canvas.unregister(this);
        }
    }

    private void setGameLoop(GameLoop newGameLoop)
    {
        // unregister from the previous game loop
        unsetGameLoop();

        // register with the new game loop
        newGameLoop.register(this);
        gameLoop = newGameLoop;
    }

    private void unsetGameLoop()
    {
        // if we are currently registered with a game loop, unregister from it
        if(gameLoop != null)
        {
            gameLoop.unregister(this);
        }
    }
}
