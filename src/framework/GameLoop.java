package framework;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JFrame;

public class GameLoop
{
    private static final long STEP_INTERVAL = 15;

    private boolean keepLooping;

    private final Canvas canvas;

    private final Set<InputEntity> inputProviders;

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

    public GameLoop()
    {
        this(null);
    }

    //////////////////////
    // public interface //
    //////////////////////

    public void breakLoop()
    {
        // stop the game loop
        keepLooping = false;
    }

    public void loop()
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
            try
            {
                Thread.sleep(Math.max(sleepTime,0));
            }
            catch(InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public void register(InputEntity i)
    {
        inputProviders.add(i);
    }

    public void unregister(InputEntity i)
    {
        inputProviders.remove(i);
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
        for(InputEntity i : inputProviders)
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
        if(canvas != null)
        {
            canvas.repaint();
        }
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        GameLoop loop = new GameLoop(canvas);
        loop.loop();
    }
}
