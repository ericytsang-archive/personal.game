package net;

public class NullClientListener<ClientKey> implements ClientListener<ClientKey>
{
    @Override
    public void onConnect(ClientKey sock)
    {
    }

    @Override
    public void onConnectFail(ClientKey sock, Exception e)
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
