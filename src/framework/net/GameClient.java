package framework.net;

import framework.InputEntity;

import net.SelectClient;

public class GameClient extends SelectClient implements InputEntity
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
