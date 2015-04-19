package net;


public interface Host<ClientKey> extends HostListener<ClientKey>
{
    /**
     * sends a message to the client identified by the connection object.
     *
     * @param sock connection to send a message to
     * @param packet packet to send from the socket.
     */
    public abstract void sendMessage(ClientKey sock, Packet packet);
}
