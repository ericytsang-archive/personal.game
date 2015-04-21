package game;

import java.awt.Graphics;
import java.nio.ByteBuffer;

import net.Packet;
import framework.Controller;
import framework.GameEntity;

public class Gunner extends GameEntity
{
    public static final int MAX_SPEED = 10;
    private int x;
    private int y;
    private int xSpeed;
    private int ySpeed;
    private Controller ctrl;

    public Gunner(Controller ctrl, int x, int y)
    {
        super(RenderDepths.GUNNER.ordinal());
        System.out.println("Gunner Created");
        this.x = x;
        this.y = y;
        this.xSpeed = 0;
        this.ySpeed = 0;
        this.ctrl = ctrl;
    }

    @Override
    public Gunner fromBytes(byte[] data)
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
        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.putInt(x);
        buf.putInt(y);
        buf.putInt(xSpeed);
        buf.putInt(ySpeed);
        return buf.array();
    }

    @Override
    public void update()
    {
        x += xSpeed;
        y += ySpeed;

        Packet[] events = ctrl.getEvents();
        for(Packet e : events)
        {
        ByteBuffer buf = ByteBuffer.wrap(e.popData());
        switch(Command.values()[buf.getInt()])
        {
        case MOVE_U:
            ySpeed -= MAX_SPEED;
            break;
        case MOVE_D:
            ySpeed += MAX_SPEED;
            break;
        case MOVE_L:
            xSpeed -= MAX_SPEED;
            break;
        case MOVE_R:
            xSpeed += MAX_SPEED;
            break;
        }
        }
    }

    @Override
    public void render(Graphics g)
    {
        g.drawArc(x-5,y-5,10,10,0,360);
    }
}
