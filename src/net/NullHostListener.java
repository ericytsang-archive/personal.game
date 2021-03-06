package net;

public class NullHostListener<ClientKey> implements HostListener<ClientKey>
{
    @Override
    public void onOpen(ClientKey sock)
    {
    }

    @Override
    public void onError(Object obj, Exception e)
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
