package net.aibote.handler.impl;

import net.aibote.handler.ClientManager;

public class WebClientManager extends ClientManager {
    private static WebClientManager instance = new WebClientManager();
    public static ClientManager getInstance()
    {
        return instance;
    }

    private WebClientManager() { }
}
