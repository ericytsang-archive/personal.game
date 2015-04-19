package net;

public interface HostListener<ClientKey>
{
    /**
     * callback invoked when a new connection is established.
     *
     * @param conn connection that is created to communicate with the new
     *   connection.
     */
    public abstract void onOpen(ClientKey conn);

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
