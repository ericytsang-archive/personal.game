package framework;

public abstract class GameEntity implements Entity, Drawable
{
    private GameLoop gameLoop;
    private Canvas canvas;
    private int renderDepth;

    public GameEntity(int renderDepth)
    {
        // initialize instance variables from parameters
        this.renderDepth = renderDepth;
    }

    public int getRenderDepth()
    {
        return renderDepth;
    }

    public GameEntity setCanvas(Canvas newCanvas)
    {
        // unregister from previous canvas
        unsetCanvas();

        if(newCanvas != null)
        {
            newCanvas.register(this);
            canvas = newCanvas;
        }

        return this;
    }

    public GameEntity unsetCanvas()
    {
        // if we are currently registered with a game loop, unregister from it
        if(canvas != null)
        {
            canvas.unregister(this);
            canvas = null;
        }

        return this;
    }

    public GameEntity setGameLoop(GameLoop newGameLoop)
    {
        // unregister from the previous game loop
        unsetGameLoop();

        if(newGameLoop != null)
        {
            // register with the new game loop
            newGameLoop.register(this);
            gameLoop = newGameLoop;
        }

        return this;
    }

    public GameEntity unsetGameLoop()
    {
        // if we are currently registered with a game loop, unregister from it
        if(gameLoop != null)
        {
            gameLoop.unregister(this);
            gameLoop = null;
        }

        return this;
    }
}
