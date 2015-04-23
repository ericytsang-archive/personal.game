package net;

public class NullClient<ClientKey> implements Client<ClientKey>
{
    @Override
    public ClientKey connect(String remoteAddr, int remotePort)
    {
        return null;
    }

    @Override
    public void disconnect(ClientKey sock)
    {
    }

    @Override
    public void sendMessage(ClientKey sock, Packet packet)
    {
    }
}
