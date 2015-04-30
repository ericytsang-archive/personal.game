package game;

import java.awt.Color;
import java.awt.Graphics;
import java.nio.ByteBuffer;

import net.Packet;
import framework.Controller;
import framework.GameEntity;
import framework.net.ServerController;

public class Gunner extends GameEntity
{
    public static final int MAX_SPEED = 7;
    public static final int JUMP_COMMAND_INTERVAL = 500;
    public static final int DRAW_RADIUS = 20;
    public static final int DEMI_DRAW_RADIUS = DRAW_RADIUS/2;
    private int x;
    private int y;
    private int xSpeed;
    private int ySpeed;
    private int targetXSpeed;
    private int targetYSpeed;
    private Controller ctrl;
    private int sendJumpCommandTimer;
    private Color renderColor;

    public Gunner(Controller ctrl, int x, int y)
    {
        super(RenderDepths.GUNNER.ordinal());
        this.x = x;
        this.y = y;
        this.xSpeed = 0;
        this.ySpeed = 0;
        this.targetXSpeed = 0;
        this.targetYSpeed = 0;
        this.ctrl = ctrl;

        float hue = (float) (Math.random()*256F);
        float saturation = 0.9f;
        float luminance = 1.0f;
        this.renderColor = Color.getHSBColor(hue,saturation,luminance);

        this.sendJumpCommandTimer = JUMP_COMMAND_INTERVAL;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public Color getRenderColor()
    {
        return renderColor;
    }

    @Override
    public Gunner fromBytes(byte[] data)
    {
        ByteBuffer buf = ByteBuffer.wrap(data);
        x = buf.getInt();
        y = buf.getInt();
        xSpeed = buf.getInt();
        ySpeed = buf.getInt();
        renderColor = new Color(buf.getInt());
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
        buf.putInt(renderColor.getRGB());
        return buf.array();
    }

    @Override
    public void update()
    {
        // move the gunner by current xSpeed and ySpeed
        x += xSpeed;
        y += ySpeed;

        // step xSpeed and ySpeed towards targetXSpeed and targetYSpeed
        if(xSpeed < targetXSpeed)
        {
            ++xSpeed;
        }
        if(ySpeed < targetYSpeed)
        {
            ++ySpeed;
        }
        if(xSpeed > targetXSpeed)
        {
            --xSpeed;
        }
        if(ySpeed > targetYSpeed)
        {
            --ySpeed;
        }

        // parse and handle packets
        Packet[] events = ctrl.getEvents();
        for(Packet e : events)
        {
        ByteBuffer buf = ByteBuffer.wrap(e.peekData());
        e = e.popData();
        switch(Command.values()[buf.getInt()])
        {
        case MOVE_U:
            targetYSpeed -= MAX_SPEED;
            break;
        case MOVE_D:
            targetYSpeed += MAX_SPEED;
            break;
        case MOVE_L:
            targetXSpeed -= MAX_SPEED;
            break;
        case MOVE_R:
            targetXSpeed += MAX_SPEED;
            break;
        case JUMP:
            x = buf.getInt();
            y = buf.getInt();
            xSpeed = buf.getInt();
            ySpeed = buf.getInt();
            break;
        default:
            throw new RuntimeException("default case triggered");
        }
        }
    }

    @Override
    public void serverUpdate()
    {
        if(--sendJumpCommandTimer < 0)
        {
            // reset jump timer
            sendJumpCommandTimer = JUMP_COMMAND_INTERVAL;

            // create the jump command packet, and send it
            ByteBuffer payload = ByteBuffer.allocate(5*4);
            Packet packet = new Packet();

            payload.putInt(Command.JUMP.ordinal());
            payload.putInt(x);
            payload.putInt(y);
            payload.putInt(xSpeed);
            payload.putInt(ySpeed);

            ServerController svrCtrl = (ServerController) ctrl;
            svrCtrl.update(packet.pushData(payload.array()));
        }
    }

    @Override
    public void render(Graphics g)
    {
        g.setColor(renderColor);
        g.fillArc(x-DEMI_DRAW_RADIUS,y-DEMI_DRAW_RADIUS,DRAW_RADIUS,DRAW_RADIUS,0,360);
    }
}
