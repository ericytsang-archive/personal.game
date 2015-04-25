package game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.Set;

import framework.Serializable;
import net.Packet;

public class ClientCommand extends framework.net.Entity implements KeyListener,MouseListener,MouseMotionListener,framework.Entity 
{
    private static final int SHOOT_INTERVAL = 5;

    private final Set<Integer> pressedKeys;

    private final Set<Integer> pressedMouseButtons;

    private int mouseX;

    private int mouseY;

    private int shootTimer;

    private boolean isShooting;

    //////////////////
    // constructors //
    //////////////////

    public ClientCommand(int id)
    {
        super(id,PairType.SVRCMD_CLNTCMD);
        pressedKeys = new LinkedHashSet<>();
        pressedMouseButtons = new LinkedHashSet<>();
        isShooting = false;
    }

    /////////////////
    // KeyListener //
    /////////////////

    @Override
    public void keyPressed(KeyEvent e)
    {
        // check if the key is already pressed. if it isn't continue, short
        // circuit otherwise
        if(pressedKeys.contains(e.getKeyCode()))
        {
            return;
        }
        else
        {
            pressedKeys.add(e.getKeyCode());
        }

        // create the command packet, and send it
        ByteBuffer payload = ByteBuffer.allocate(4);
        Packet packet = new Packet();

        switch(e.getKeyCode())
        {
        case KeyEvent.VK_W:
            payload.putInt(Command.MOVE_U.ordinal());
            break;
        case KeyEvent.VK_A:
            payload.putInt(Command.MOVE_L.ordinal());
            break;
        case KeyEvent.VK_S:
            payload.putInt(Command.MOVE_D.ordinal());
            break;
        case KeyEvent.VK_D:
            payload.putInt(Command.MOVE_R.ordinal());
            break;
        default:
            // if there is no command, don't send anything
            return;
        }

        update(packet.pushData(payload.array()));
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        // remove the key from set of pressed keys
        pressedKeys.remove(e.getKeyCode());

        // create the command packet, and send it
        ByteBuffer payload = ByteBuffer.allocate(4);
        Packet packet = new Packet();

        switch(e.getKeyCode())
        {
        case KeyEvent.VK_W:
            payload.putInt(Command.MOVE_D.ordinal());
            break;
        case KeyEvent.VK_A:
            payload.putInt(Command.MOVE_R.ordinal());
            break;
        case KeyEvent.VK_S:
            payload.putInt(Command.MOVE_U.ordinal());
            break;
        case KeyEvent.VK_D:
            payload.putInt(Command.MOVE_L.ordinal());
            break;
        case KeyEvent.VK_SPACE:
            System.out.println("spacePressed");
            isShooting = !isShooting;
            return;
        default:
            // if there is no command, don't send anything
            return;
        }

        update(packet.pushData(payload.array()));
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
        // do nothing
    }

    ///////////////////
    // MouseListener //
    ///////////////////

    @Override
    public void mousePressed(MouseEvent e)
    {
        // check if the button is already pressed. if it isn't continue, short
        // circuit otherwise
        if(pressedKeys.contains(e.getButton()))
        {
            return;
        }
        else
        {
            pressedKeys.add(e.getButton());
        }

        // set the shooting flag
        if(e.getButton() == MouseEvent.BUTTON1)
        {
            isShooting = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        // remove the button from set of pressed button
        pressedMouseButtons.add(e.getButton());

        // unset the shooting flag
        if(e.getButton() == MouseEvent.BUTTON1)
        {
            isShooting = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // do nothing
    }

    /////////////////////////
    // MouseMotionListener //
    /////////////////////////

    @Override
    public void mouseDragged(MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        // record the mouse's x and y coordinates
        mouseX = e.getX();
        mouseY = e.getY();
    }

    //////////////////////////
    // framework.net.Entity //
    //////////////////////////

    @Override
    public Packet getRegisterPacket()
    {
        return new Packet();
    }

    @Override
    public void onUpdate(Packet packet)
    {
        System.out.println(new String(packet.toBytes()));
    }

    @Override
    public void onUnregister(Packet packet)
    {
        // do nothing
    }

    //////////////////////
    // framework.Entity //
    //////////////////////

    @Override
    public Serializable fromBytes(byte[] data)
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public byte[] toBytes()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void update()
    {
        if(isShooting && --shootTimer < 0)
        {
            // reset the shoot timer
            System.out.println("fire");
            shootTimer = SHOOT_INTERVAL;

            // create and send a create bullet command
            // create the command packet, and send it
            ByteBuffer payload = ByteBuffer.allocate(3*4);
            Packet packet = new Packet();

            payload.putInt(Command.MAKE_BULLET.ordinal());
            payload.putInt(mouseX);
            payload.putInt(mouseY);

            update(packet.pushData(payload.array()));
        }
    }

    @Override
    public void serverUpdate()
    {
        throw new RuntimeException("not implemented");
    }
}
