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

    private final Set<InputEntity> inputProvidersToRemove;

    private final Set<Entity> entities;

    private final Set<Entity> entitiesToRemove;

    //////////////////
    // constructors //
    //////////////////

    public GameLoop(Canvas canvas)
    {
        // initialize instance variables
        this.keepLooping = true;
        this.canvas = canvas;
        this.inputProviders = new LinkedHashSet<>();
        this.inputProvidersToRemove = new LinkedHashSet<>();
        this.entities = new LinkedHashSet<>();
        this.entitiesToRemove = new LinkedHashSet<>();
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
        inputProvidersToRemove.add(i);
    }

    public void register(Entity e)
    {
        entities.add(e);
    }

    public void unregister(Entity e)
    {
        entitiesToRemove.add(e);
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

        for(InputEntity i : inputProvidersToRemove)
        {
            inputProviders.remove(i);
        }

        inputProvidersToRemove.clear();
    }

    private final void update()
    {
        for(Entity e : entities)
        {
            e.update();
        }

        for(Entity e : entitiesToRemove)
        {
            entities.remove(e);
        }

        entitiesToRemove.clear();
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
