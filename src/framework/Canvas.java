package framework;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Comparator;
import java.util.LinkedHashSet;
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

    private final Set<Drawable> drawablesToRemove;

    public Canvas()
    {
        this.drawables = new TreeSet<Drawable>(new DrawableComparator());
        this.drawablesToRemove = new LinkedHashSet<>();
        setBackground(Color.BLACK);
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
        drawablesToRemove.add(drawable);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        for(Drawable d : drawables)
        {
            d.render(g);
        }

        for(Drawable d : drawablesToRemove)
        {
            drawables.remove(d);
        }

        drawablesToRemove.clear();
    }

    ////////////////////////
    // DrawableComparator //
    ////////////////////////

    private class DrawableComparator implements Comparator<Drawable>
    {
        public int compare(Drawable e1, Drawable e2)
        {
            int ret = e1.getRenderDepth()-e2.getRenderDepth();
            if(ret == 0)
            {
                ret = e1.hashCode() - e2.hashCode();
            }
            return ret;
        }
    }
}
