package framework.net;

import framework.InputEntity;

import net.SelectServer;

public class GameServer extends SelectServer implements InputEntity
{
    ///////////////////
    // InputProvider //
    ///////////////////

    @Override
    public void processInputs()
    {
        handleMessages(this);
    }
}
