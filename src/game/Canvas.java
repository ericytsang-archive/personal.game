package game;

import java.awt.Graphics;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;

public class Canvas extends JPanel
{
    /**
     * the serialVersionUID of this component
     */
    private static final long serialVersionUID = 1L;

    private final Set<Drawable> drawables;

    public Canvas()
    {
        this.drawables = new TreeSet<>(new DrawableComparator());
    }

    //////////////////////
    // public interface //
    //////////////////////

    public void register(Drawable drawable)
    {
        drawables.add(drawable);
    }

    public void unregister(Drawable drawable)
    {
        drawables.remove(drawable);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        for(Drawable d : drawables)
        {
            d.render(g);
        }
    }

    ////////////////////////
    // DrawableComparator //
    ////////////////////////

    private class DrawableComparator implements Comparator<Drawable>
    {
        public int compare(Drawable e1, Drawable e2)
        {
            return e1.getRenderDepth()-e2.getRenderDepth();
        }
    }
}