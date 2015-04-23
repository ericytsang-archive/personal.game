package net;

public class NullServerListener<ClientKey,ServerKey> implements ServerListener<ClientKey,ServerKey>
{
    @Override
    public void onAccept(ClientKey sock)
    {
    }

    @Override
    public void onAcceptFail(ServerKey sock, Exception e)
    {
    }

    @Override
    public void onListenFail(ServerKey sock, Exception e)
    {
    }

    @Override
    public void onMessage(ClientKey sock, Packet packet)
    {
    }

    @Override
    public void onClose(ClientKey sock, boolean remote)
    {
    }
}
