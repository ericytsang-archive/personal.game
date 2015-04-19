package net;

public interface Client<ClientKey> extends Host<ClientKey>
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
     * @param conn connection to disconnect, and close.
     */
    public abstract void disconnect(ClientKey conn);

    /**
     * callback invoked when a new connection is established with the server.
     *
     * @param conn connection that is created to communicate with the new
     *   connection.
     */
    public abstract void onConnect(ClientKey conn);

    /**
     * callback invoked when a connection attempting to connect fails to
     *   connect.
     *
     * @param conn   connection that is created to communicate with the new
     *   connection.
     * @param e   exception that occurred on the connection.
     */
    public abstract void onConnectFail(ClientKey conn, Exception e);
}
