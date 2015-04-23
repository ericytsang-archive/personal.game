package net;

public class HostAdapter<ClientKey,ServerKey> implements Host<ClientKey>
{
    private Client<ClientKey> clientAdaptee;

    private Server<ClientKey,ServerKey> serverAdaptee;

    ///////////////////
    // constructors  //
    ///////////////////

    public HostAdapter(Client<ClientKey> clientAdaptee)
    {
        this(clientAdaptee,null);
    }

    public HostAdapter(Server<ClientKey,ServerKey> serverAdaptee)
    {
        this(null,serverAdaptee);
    }

    public HostAdapter(Client<ClientKey> clientAdaptee, Server<ClientKey,ServerKey> serverAdaptee)
    {
        this.clientAdaptee = (clientAdaptee != null) ? clientAdaptee : new NullClient<ClientKey>();
        this.serverAdaptee = (serverAdaptee != null) ? serverAdaptee : new NullServer<ClientKey,ServerKey>();
    }

    /////////////////////
    // Host<ClientKey> //
    /////////////////////

    @Override
    public void sendMessage(ClientKey sock, Packet packet)
    {
        clientAdaptee.sendMessage(sock,packet);
        serverAdaptee.sendMessage(sock,packet);
    }
}
