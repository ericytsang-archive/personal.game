package net;

public class NullHostListener<ClientKey> implements HostListener<ClientKey>
{
    @Override
    public void onOpen(ClientKey sock)
    {
        // do nothing
    }

    @Override
    public void onMessage(ClientKey sock, Packet packet)
    {
        // do nothing
    }

    @Override
    public void onClose(ClientKey sock, boolean remote)
    {
        // do nothing
    }
}
