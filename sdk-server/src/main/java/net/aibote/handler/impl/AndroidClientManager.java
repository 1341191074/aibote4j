package net.aibote.handler.impl;

import net.aibote.handler.ClientManager;

public class AndroidClientManager extends ClientManager {
    private static AndroidClientManager instance = new AndroidClientManager();
    public static ClientManager getInstance()
    {
        return instance;
    }

    private AndroidClientManager() { }
}
