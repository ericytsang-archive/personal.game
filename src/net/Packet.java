package net;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Stack;

/**
 * fancy wrapper for a stack so that data can be pushed onto the packet, and
 *   popped off of it. it is useful when things want to push their own headers
 *   onto the packet, so they can be popped off later.
 *
 * provides methods to serialize the {Packet} into a byte array, and deserialize
 *   a byte array back into a {Packet}. these methods are used when the {Packet}
 *   is being sent and received on the network.
 *
 * a {Packet} can be sent over the network using the {Client} and {Server}
 *   subclasses.
 *
 * @author Eric Tsang
 */
public class Packet implements framework.Serializable
{
    /**
     * the packet's internal stack of data, where data is pushed onto, and
     *   popped off.
     */
    private final Stack<byte[]> packetData;

    /**
     * length of the actual data encapsulated in this packet, not including
     *   delimiters, or escape characters. just pure data.
     */
    private int packetLength;

    //////////////////
    // constructors //
    //////////////////

    /**
     * instantiates a new packet instance.
     */
    public Packet()
    {
        packetData = new Stack<>();
        packetLength = 0;
    }

    //////////////////////
    // public interface //
    //////////////////////

    /**
     * parses {data}, and replaces the data in the packet with the parsed data.
     *
     * @param    data   data to parse into the packet. this data should have
     *   been created using the Packet::toBytes method.
     *
     * @return   returns a this pointer.
     */
    public Packet fromBytes(byte[] data)
    {
        return fromBytes(data,data.length);
    }

    /**
     * parses {data}, and replaces the data in the packet with the parsed data.
     *
     * @param    data   data to parse into the packet. this data should have
     *   been created using the Packet::toBytes method.
     *
     * @return   returns a this pointer.
     */
    public Packet fromBytes(byte[] data, int length)
    {
        // reset the state of this packet
        packetData.clear();
        packetLength = 0;

        // go through the raw data, and put it into our reversed stack
        ByteBuffer rawData = ByteBuffer.wrap(data,0,length);
        Stack<byte[]> reversedSections = new Stack<>();
        while(rawData.hasRemaining())
        {
            byte[] sectionData = new byte[rawData.getInt()];
            rawData.get(sectionData,0,sectionData.length);
            reversedSections.push(sectionData);
        }

        // move the data from the reversed stack onto the real one
        Packet ret = new Packet();
        while(reversedSections.size() > 0)
        {
            ret = ret.pushData(reversedSections.pop());
        }

        // return the packet
        return ret;
    }

    /**
     * returns the byte version of the packet that can be used to transmit "over
     *   the wire".
     *
     * each section of data is preceded by a 32-bit integer that specifies its
     *   length.
     *
     * @return   the byte version of the packet that can be used to transmit
     *   "over the wire".
     */
    public byte[] toBytes()
    {
        // allocate all the bytes we need to hold the packet data
        ByteBuffer packet = ByteBuffer.allocate(
            packetLength+packetData.size()*4);

        // go through the stack, and write it to the byte array
        for(int i = packetData.size()-1; i >= 0; --i)
        {
            byte[] data = packetData.get(i);
            packet.putInt(data.length);
            packet.put(data);
        }

        // return the packet
        return packet.array();
    }

    /**
     * adds {data} as the new header of the packet, and the previous data
     *   becomes "payload".
     *
     * @param    data   the new data to add to the packet as header data.
     *
     * @return   returns a this pointer, so calls to this method can be chained.
     */
    public Packet pushData(byte[] data)
    {
        // create a copy of this packet
        Packet p = new Packet().fromBytes(toBytes());

        // update book keeping variables
        p.packetLength += data.length;

        // put the data on our stack
        p.packetData.push(data);

        // return this, so we can chain stuff
        return p;
    }

    /**
     * returns the data of the current header of the packet, and removes it from
     *   the packet.
     *
     * @return   the data of the current header of the packet, and removes it
     *   from the packet.
     */
    public Packet popData()
    {
        // create a copy of this packet
        Packet p = new Packet().fromBytes(toBytes());

        // pop the the data from the stack & parse it
        byte[] data = p.packetData.pop();

        // update book keeping variables
        p.packetLength -= data.length;

        // return the data
        return p;
    }

    /**
     * returns the data of the current header of the packet, without removing it
     *   from the packet.
     *
     * @return   the data of the current header of the packet, without removing
     *   it from the packet.
     */
    public byte[] peekData()
    {
        // peek the the data from the stack, parse it & return it
        return packetData.peek();
    }

    public static void main(String[] args)
    {
        Packet p = new Packet();
        p.pushData(new byte[] {0,1,2,3,4,5});
        p.pushData(new byte[] {1,1,2,3,4,5});
        p.pushData(new byte[] {2,1,2,3,4,5});
        p.pushData(new byte[] {3,1,2,3,4,5});

        System.out.println(Arrays.toString(p.toBytes()));

        System.out.println(Arrays.toString(p.fromBytes(p.toBytes()).toBytes()));

        System.out.println(Arrays.toString(p.popData().peekData()));
        System.out.println(Arrays.toString(p.popData().peekData()));
        System.out.println(Arrays.toString(p.popData().peekData()));
        System.out.println(Arrays.toString(p.popData().peekData()));
    }
}
