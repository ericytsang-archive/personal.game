package net;

public interface Client<ClientKey>
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
    public abstract ClientKey connect(String remoteAddr, int remotePort);

    /**
     * disconnects the specified connection.
     *
     * @param sock connection to disconnect, and close.
     */
    public abstract void disconnect(ClientKey sock);

    /**
     * sends a message to the client identified by the connection object.
     *
     * @param sock connection to send a message to
     * @param packet packet to send from the socket.
     */
    public abstract void sendMessage(ClientKey sock, Packet packet);
}
