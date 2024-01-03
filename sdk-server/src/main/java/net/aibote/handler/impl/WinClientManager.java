package net.aibote.handler.impl;

import net.aibote.handler.ClientManager;

public class WinClientManager extends ClientManager {
    private static WinClientManager instance = new WinClientManager();

    public static ClientManager getInstance() {
        return instance;
    }

    private WinClientManager() {
    }
}
