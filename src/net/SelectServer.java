package net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SelectServer implements Server
{
    /**
     * the select thread of this class. isn't instantiated directly, instead,
     *   get the instane's select thread using the getSelectThread method.
     */
    private SelectThread selectThread;

    @Override
    public ServerSocket startListening(int serverPort)
    {
        // open a server socket
        ServerSocket sock;
        try
        {
            sock = new ServerSocket();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        // add the socket to the select thread
        getSelectThread().addServerSocket(sock,serverPort);

        // return...
        return sock;
    }

    @Override
    public void stopListening(ServerSocket sock)
    {
        // remove the socket from the select thread
        getSelectThread().removeServerSocket(sock);
    }

    @Override
    public void sendMessage(Socket sock, Packet packet)
    {
        synchronized(sock)
        {
            try
            {
                byte[] packetData = packet.toBytes();
                DataOutputStream os = new DataOutputStream(
                    sock.getOutputStream());
                os.writeInt(packetData.length);
                os.write(packetData);
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onAcceptFail(ServerSocket sock, Exception e)
    {
    }

    @Override
    public void onListenFail(ServerSocket sock, Exception e)
    {
    }

    @Override
    public void onAccept(Socket sock)
    {
    }

    @Override
    public void onMessage(Socket sock, Packet packet)
    {
    }

    @Override
    public void onClose(Socket sock, boolean remote)
    {
    }

    ///////////////////////
    // private interface //
    ///////////////////////

    private SelectThread getSelectThread()
    {
        if(selectThread == null)
        {
            selectThread = new SelectThread();
        }
        return selectThread;
    }
}
