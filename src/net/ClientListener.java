package net;

public interface ClientListener<ClientKey>
{
    /**
     * callback invoked when a new connection is established with the server.
     *
     * @param sock connection that is created to communicate with the new
     *   connection.
     */
    public abstract void onConnect(ClientKey sock);

    /**
     * callback invoked when a connection attempting to connect fails to
     *   connect.
     *
     * @param sock   connection that is created to communicate with the new
     *   connection.
     * @param e   exception that occurred on the connection.
     */
    public abstract void onConnectFail(ClientKey sock, Exception e);

    /**
     * callback invoked when a message from a connection is received.
     *
     * @param sock socket that the message was received from.
     * @param packet packet received from the socket.
     */
    public abstract void onMessage(ClientKey sock, Packet packet);

    /**
     * callback invoked when the socket is closed by either the server, or the
     *   client.
     *
     * @param sock socket that was closed.
     * @param remote true if the socket was closed by the remote host; false
     *   otherwise.
     */
    public abstract void onClose(ClientKey sock, boolean remote);
}
