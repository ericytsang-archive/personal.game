package net;

public interface Server<ServerKey,ClientKey> extends Host<ClientKey>
{
    /**
     * starts the server, and makes it listening for connections on the passed
     *   port.
     *
     * @param serverPort port to open for listening.
     *
     * @return the {ServerSocket} used to listen for connection requests.
     */
    public abstract ServerKey startListening(int serverPort);

    /**
     * stops the server from listening on the specified port, and accepting
     *   connections.
     *
     * @param socket
     */
    public abstract void stopListening(ServerKey socket);

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
}
