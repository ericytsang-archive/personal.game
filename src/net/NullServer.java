package net;

public class NullServer<ClientKey,ServerKey> implements Server<ClientKey,ServerKey>
{
    @Override
    public ServerKey startListening(int serverPort)
    {
        return null;
    }

    @Override
    public void stopListening(ServerKey socket)
    {
    }

    @Override
    public void sendMessage(ClientKey sock, Packet packet)
    {
    }
}
