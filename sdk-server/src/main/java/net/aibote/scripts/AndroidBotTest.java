package net.aibote.scripts;

import net.aibote.sdk.AndroidBot;

public class AndroidBotTest extends AndroidBot {
    @Override
    public String getScriptName() {
        return "测试脚本";
    }

    @Override
    public void doScript() {
        this.sleep(5000);//静默5秒
        String androidId = this.getAndroidId();
        System.out.println(androidId);
    }
}
