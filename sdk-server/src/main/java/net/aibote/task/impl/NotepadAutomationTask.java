package net.aibote.task.impl;

import lombok.Builder;
import lombok.Data;
import net.aibote.sdk.AbstractPlatformBot;
import net.aibote.sdk.WinBot;
import net.aibote.sdk.factory.BotFactory;
import net.aibote.sdk.options.Mode;
import net.aibote.task.TaskDefinition;

import java.util.Set;

/**
 * Windows记事本自动化任务示例
 * 演示如何创建具体的任务实现
 *
 * @author AIBoTe
 * @version 1.0
 * @since 2026-01-19
 */
@Data
@Builder
public class NotepadAutomationTask implements TaskDefinition {
    
    private String taskName;
    private String scriptName;
    private String description;
    
    @Override
    public Set<BotFactory.BotType> getSupportedBotTypes() {
        return Set.of(BotFactory.BotType.WIN);
    }
    
    @Override
    public TaskExecutor getTaskExecutor() {
        return this::executeTask;
    }
    
    /**
     * 执行记事本自动化任务
     * @param bot 机器人实例
     * @throws Exception 执行异常
     */
    private void executeTask(AbstractPlatformBot bot) throws Exception {
        // 确保是WinBot类型
        if (!(bot instanceof WinBot)) {
            throw new IllegalArgumentException("此任务仅支持Windows机器人");
        }
        
        WinBot winBot = (WinBot) bot;

        Thread.sleep(3000);//静默5秒

        String cabinetWClass = winBot.findWindow("CabinetWClass", "");
        System.out.println(cabinetWClass);

        winBot.moveMouse("", 100, 100, Mode.front,"");
    }
}