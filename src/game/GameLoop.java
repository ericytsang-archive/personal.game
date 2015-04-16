package game;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JFrame;

public class GameLoop
{
    private static final long STEP_INTERVAL = 15;

    private boolean keepLooping;

    private final Canvas canvas;

    private final Set<InputProvider> inputProviders;

    private final Set<Entity> entities;

    //////////////////
    // constructors //
    //////////////////

    public GameLoop(Canvas canvas)
    {
        // initialize instance variables
        this.keepLooping = true;
        this.canvas = canvas;
        this.inputProviders = new LinkedHashSet<>();
        this.entities = new LinkedHashSet<>();
    }

    //////////////////////
    // public interface //
    //////////////////////

    public void breakLoop()
    {
        // stop the game loop
        keepLooping = false;
    }

    public void loop() throws InterruptedException
    {
        // do the game loop
        keepLooping = true;
        while(keepLooping)
        {
            double start = System.currentTimeMillis();
            processInputs();
            update();
            render();

            long sleepTime = (long)start+STEP_INTERVAL-System.currentTimeMillis();
            Thread.sleep(Math.max(sleepTime,0));
        }
    }

    public void register(Entity e)
    {
        entities.add(e);
    }

    public void unregister(Entity e)
    {
        entities.remove(e);
    }

    ///////////////////////
    // private interface //
    ///////////////////////

    private final void processInputs()
    {
        for(InputProvider i : inputProviders)
        {
            i.processInputs();
        }
    }

    private final void update()
    {
        for(Entity e : entities)
        {
            e.update();
        }
    }

    private final void render()
    {
        canvas.repaint();
    }

    //////////
    // main //
    //////////

    public static void main(String[] args) throws InterruptedException
    {
        JFrame frame = new JFrame("Awesome Game");
        Canvas canvas = new Canvas();

        frame.setContentPane(canvas);
        frame.setSize(400,400);
        frame.setVisible(true);

        GameLoop loop = new GameLoop(canvas);
        loop.loop();
    }
}
