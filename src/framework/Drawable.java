package framework;

import java.awt.Graphics;

public interface Drawable
{
    public void render(Graphics g);
    public int getRenderDepth();
}
