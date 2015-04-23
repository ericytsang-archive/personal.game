package game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.Set;

import net.Packet;

public class ClientCommand extends framework.net.Entity implements KeyListener, MouseListener
{
    private Set<Integer> pressedKeys;

    //////////////////
    // constructors //
    //////////////////

    public ClientCommand(int id, PairType pairType)
    {
        super(id,pairType);
        pressedKeys = new LinkedHashSet<>();
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
    public void mouseReleased(MouseEvent arg0)
    {
        
    }

    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent arg0)
    {
        // do nothing
    }

    @Override
    public void mouseExited(MouseEvent arg0)
    {
        // do nothing
    }

    @Override
    public void mousePressed(MouseEvent arg0)
    {
        // do nothing
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
}