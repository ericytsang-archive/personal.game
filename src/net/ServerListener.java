package net;

public interface ServerListener<ClientKey,ServerKey>
{
    /**
     * callback invoked when a new connection is established with the server.
     *
     * @param sock socket that is created to communicate with the new
     *   connection.
     */
    public abstract void onAccept(ClientKey sock);

    /**
     * callback invoked when the call to accept a new connection fails.
     *
     * @param sock socket that got the exception.
     * @param e exception that occurred on the socket.
     */
    public abstract void onAcceptFail(ServerKey sock, Exception e);

    /**
     * invoked when a listening socket somehow gets an exception.
     *
     * @param sock socket that got the exception.
     * @param e exception that occurred on the socket.
     */
    public abstract void onListenFail(ServerKey sock, Exception e);

    /**
     * callback invoked when a message from a connection is received.
     *
     * @param conn socket that the message was received from.
     * @param packet packet received from the socket.
     */
    public abstract void onMessage(ClientKey conn, Packet packet);

    /**
     * callback invoked when the socket is closed by either the server, or the
     *   client.
     *
     * @param conn socket that was closed.
     * @param remote true if the socket was closed by the remote host; false
     *   otherwise.
     */
    public abstract void onClose(ClientKey conn, boolean remote);
}
