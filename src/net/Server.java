package net;

public interface Server<ClientKey,ServerKey>
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
     * sends a message to the client identified by the connection object.
     *
     * @param sock connection to send a message to
     * @param packet packet to send from the socket.
     */
    public abstract void sendMessage(ClientKey sock, Packet packet);
}
