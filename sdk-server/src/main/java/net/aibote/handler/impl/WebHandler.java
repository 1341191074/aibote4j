package net.aibote.handler.impl;

import net.aibote.handler.BotHandler;
import net.aibote.handler.ClientManager;

public class WebHandler extends BotHandler {
    @Override
    public ClientManager getClientManager() {
        return ClientManager.getInstance();
    }
}
