package game;

import java.awt.Color;
import java.awt.Graphics;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.Packet;
import framework.Controller;
import framework.net.Mux;
import framework.net.ServerController;

public class Bullet extends framework.GameEntity
{
    public static final int BULLET_SPEED = 10;
    private float x;
    private float y;
    private float xSpeed;
    private float ySpeed;
    private int life;
    private Controller ctrl;
    private Color renderColor;

    public Bullet(Controller ctrl, int x, int y, double angle, Color renderColor)
    {
        super(RenderDepths.BULLET.ordinal());
        this.ctrl = ctrl;
        this.x = x;
        this.y = y;
        this.life = 60;
        angle += Math.random()*.05-.025;
        this.xSpeed = (float) (Math.cos(angle)*BULLET_SPEED);
        this.ySpeed = (float) (Math.sin(angle)*BULLET_SPEED);
        this.renderColor = renderColor;
    }

    @Override
    public void update()
    {
        x += xSpeed;
        y += ySpeed;

        Packet[] events = ctrl.getEvents();
        if(events.length > 0)
        {
            unsetCanvas();
            unsetGameLoop();
            framework.net.Entity netEntity = (framework.net.Entity) ctrl;
            Mux.<SocketChannel>getInstance().unregisterWithAll(netEntity,new Packet());
            ctrl = null;
        }
    }

    @Override
    public void serverUpdate()
    {
        if(--life < 0)
        {
            ServerController svrCtrl = (ServerController) ctrl;
            svrCtrl.update(new Packet());
        }
    }

    @Override
    public Bullet fromBytes(byte[] data)
    {
        ByteBuffer buf = ByteBuffer.wrap(data);
        x = buf.getFloat();
        y = buf.getFloat();
        xSpeed = buf.getFloat();
        ySpeed = buf.getFloat();
        renderColor = new Color(buf.getInt());
        return this;
    }

    @Override
    public byte[] toBytes()
    {
        ByteBuffer buf = ByteBuffer.allocate(5*4);
        buf.putFloat(x);
        buf.putFloat(y);
        buf.putFloat(xSpeed);
        buf.putFloat(ySpeed);
        buf.putInt(renderColor.getRGB());
        return buf.array();
    }

    @Override
    public void render(Graphics g)
    {
        g.setColor(renderColor);
        g.drawLine((int)(x-xSpeed),(int)(y-ySpeed),(int)x,(int)y);
    }
}
