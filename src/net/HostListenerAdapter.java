package net;

public class HostListenerAdapter<ClientKey,ServerKey> implements ClientListener<ClientKey>, ServerListener<ClientKey,ServerKey>
{
    private HostListener<ClientKey> observer;

    private final HostListener<ClientKey> NULL_OBJECT =
            new NullHostListener<ClientKey>();

    //////////////////////
    // public interface //
    //////////////////////

    public HostListenerAdapter<ClientKey,ServerKey> setObserver(HostListener<ClientKey> observer)
    {
        this.observer = (observer != null) ? observer : NULL_OBJECT;
        return this;
    }

    //////////////////////////////////////////////////////////////////
    // ClientListener<ClientKey> & ServerListener<ClientKey,Object> //
    //////////////////////////////////////////////////////////////////

    @Override
    public void onAccept(ClientKey sock)
    {
        observer.onOpen(sock);
    }

    @Override
    public void onAcceptFail(ServerKey sock, Exception e)
    {
        observer.onError(sock,e);
    }

    @Override
    public void onListenFail(ServerKey sock, Exception e)
    {
        observer.onError(sock,e);
    }

    @Override
    public void onConnect(ClientKey sock)
    {
        observer.onOpen(sock);
    }

    @Override
    public void onConnectFail(ClientKey sock, Exception e)
    {
        observer.onError(sock,e);
    }

    @Override
    public void onMessage(ClientKey sock, Packet packet)
    {
        observer.onMessage(sock,packet);
    }

    @Override
    public void onClose(ClientKey sock, boolean remote)
    {
        observer.onClose(sock,remote);
    }
}
