package net.aibote.handler.impl;

import net.aibote.handler.BotHandler;
import net.aibote.handler.ClientManager;
import net.aibote.sdk.factory.BotFactory;

public class AndroidHandler extends BotHandler {
    @Override
    public ClientManager getClientManager() {
        return ClientManager.getInstance();
    }
    
    @Override
    protected BotFactory.BotType getBotType() {
        return BotFactory.BotType.ANDROID;
    }
}
