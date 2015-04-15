package net;

import java.net.Socket;

public interface Client
{
    /**
     * initiates a new connection to the remote server at the address
     *   {remoteAddr}:{remotePort}. when the socket connects, the onConnect
     *   callback will be invoked. if the socket fails to connect, the
     *   {onConnectFail} callback is invoked.
     *
     * @param    remoteAddr   IP address of the remote host.
     * @param    remotePort   port number of the remote host to connect to.
     *
     * @return   returns the socket that is performing the connect call.
     */
    public abstract Socket connect(String remoteAddr, int remotePort);

    /**
     * disconnects the specified socket.
     *
     * @param socket socket to disconnect, and close.
     */
    public abstract void disconnect(Socket socket);


    /**
     * sends a message to the client identified by the connection object.
     *
     * @param sock connection to send a message to
     * @param packet packet to send from the socket.
     */
    public abstract void sendMessage(Socket sock, Packet packet);

    /**
     * callback invoked when a new connection is established with the server.
     *
     * @param conn socket that is created to communicate with the new
     *   connection.
     */
    public abstract void onConnect(Socket conn);

    /**
     * callback invoked when a socket attempting to connect fails to connect.
     *
     * @param conn   socket that is created to communicate with the new
     *   connection.
     * @param e   exception that occurred on the socket.
     */
    public abstract void onConnectFail(Socket conn, Exception e);

    /**
     * callback invoked when a message from a connection is received.
     *
     * @param sock socket that the message was received from.
     * @param packet packet received from the socket.
     */
    public abstract void onMessage(Socket sock, Packet packet);

    /**
     * callback invoked when the socket is closed by either the server, or the
     *   client.
     *
     * @param sock socket that was closed.
     * @param remote true if the socket was closed by the remote host; false
     *   otherwise.
     */
    public abstract void onClose(Socket sock, boolean remote);
}
