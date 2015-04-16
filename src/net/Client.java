package net;

public interface Client<ClientSock extends Object>
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
     * @return   returns the {Object} used as a key that identifies this
     *   connection.
     */
    public abstract ClientSock connect(String remoteAddr, int remotePort);

    /**
     * disconnects the specified connection.
     *
     * @param conn connection to disconnect, and close.
     */
    public abstract void disconnect(ClientSock conn);


    /**
     * sends a message to the client identified by the connection object.
     *
     * @param conn connection to send a message to
     * @param packet packet to send from the connection.
     */
    public abstract void sendMessage(ClientSock conn, Packet packet);

    /**
     * callback invoked when a new connection is established with the server.
     *
     * @param conn connection that is created to communicate with the new
     *   connection.
     */
    public abstract void onConnect(ClientSock conn);

    /**
     * callback invoked when a connection attempting to connect fails to
     *   connect.
     *
     * @param conn   connection that is created to communicate with the new
     *   connection.
     * @param e   exception that occurred on the connection.
     */
    public abstract void onConnectFail(ClientSock conn, Exception e);

    /**
     * callback invoked when a message from a connection is received.
     *
     * @param conn connection that the message was received from.
     * @param packet packet received from the connection.
     */
    public abstract void onMessage(ClientSock conn, Packet packet);

    /**
     * callback invoked when the connection is closed by either the server, or
     *   the client.
     *
     * @param conn connection that was closed.
     * @param remote true if the connection was closed by the remote host; false
     *   otherwise.
     */
    public abstract void onClose(ClientSock conn, boolean remote);
}
