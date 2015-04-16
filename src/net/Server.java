package net;

import java.net.ServerSocket;
import java.net.Socket;

public interface Server
{
    /**
     * starts the server, and makes it listening for connections on the passed
     *   port.
     *
     * @param serverPort port to open for listening.
     *
     * @return the {ServerSocket} used to listen for connection requests.
     */
    public abstract ServerSocket startListening(int serverPort);

    /**
     * stops the server from listening on the specified port, and accepting
     *   connections.
     *
     * @param socket
     */
    public abstract void stopListening(ServerSocket socket);

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
     * @param sock socket that is created to communicate with the new
     *   connection.
     */
    public abstract void onAccept(Socket sock);

    /**
     * callback invoked when the call to accept a new connection fails.
     *
     * @param sock socket that got the exception.
     * @param e exception that occurred on the socket.
     */
    public abstract void onAcceptFail(ServerSocket sock, Exception e);

    /**
     * invoked when a listening socket somehow gets an exception.
     *
     * @param sock socket that got the exception.
     * @param e exception that occurred on the socket.
     */
    public abstract void onListenFail(ServerSocket sock, Exception e);

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
