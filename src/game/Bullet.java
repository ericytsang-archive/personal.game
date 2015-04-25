package game;

import java.awt.Color;
import java.awt.Graphics;
import java.nio.ByteBuffer;

import framework.Controller;

public class Bullet extends framework.GameEntity
{
    private int x;
    private int y;
    private int xSpeed;
    private int ySpeed;
    private Controller ctrl;
    private Color renderColor;

    public Bullet(Controller ctrl, int x, int y, int xSpeed, int ySpeed, Color renderColor)
    {
        super(RenderDepths.BULLET.ordinal());
        this.ctrl = ctrl;
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.renderColor = renderColor;
    }

    @Override
    public void update()
    {
        x += xSpeed;
        y += ySpeed;
    }

    @Override
    public void serverUpdate()
    {
        // do nothing
    }

    @Override
    public Bullet fromBytes(byte[] data)
    {
        ByteBuffer buf = ByteBuffer.wrap(data);
        x = buf.getInt();
        y = buf.getInt();
        xSpeed = buf.getInt();
        ySpeed = buf.getInt();
        return this;
    }

    @Override
    public byte[] toBytes()
    {
        ByteBuffer buf = ByteBuffer.allocate(5*4);
        buf.putInt(x);
        buf.putInt(y);
        buf.putInt(xSpeed);
        buf.putInt(ySpeed);
        return buf.array();
    }

    @Override
    public void render(Graphics g)
    {
        g.setColor(renderColor);
        g.drawLine(x-xSpeed,y-ySpeed,x,y);
    }
}
