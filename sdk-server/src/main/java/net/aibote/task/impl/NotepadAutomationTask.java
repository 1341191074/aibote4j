package net.aibote.task.impl;

import lombok.Builder;
import lombok.Data;
import net.aibote.sdk.AbstractPlatformBot;
import net.aibote.sdk.WinBot;
import net.aibote.sdk.factory.BotFactory;
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
        
        // 任务逻辑：查找记事本窗口并执行操作
        System.out.println("开始执行记事本自动化任务...");
        
        // 1. 查找记事本窗口
        String hwnd = winBot.findWindow("CabinetWClass", "此电脑 - 文件资源管理器");
        if (hwnd == null) {
            System.out.println("未找到记事本窗口");
            return;
        }
        System.out.println("找到记事本窗口: " + hwnd);
        
        // 2. 获取窗口位置
        String windowPos = winBot.getWindowPos(hwnd);
        System.out.println("窗口位置: " + windowPos);
        
        // 3. 移动鼠标到窗口中心
        String[] pos = windowPos.split("\\|");
        if (pos.length >= 4) {
            int left = Integer.parseInt(pos[0]);
            int top = Integer.parseInt(pos[1]);
            int right = Integer.parseInt(pos[2]);
            int bottom = Integer.parseInt(pos[3]);
            
            int centerX = left + (right - left) / 2;
            int centerY = top + (bottom - top) / 2;
            
            winBot.moveMouse(hwnd, centerX, centerY, net.aibote.sdk.options.Mode.front, null);
            System.out.println("鼠标已移动到窗口中心: (" + centerX + ", " + centerY + ")");
            
            // 4. 点击窗口
            winBot.clickMouse(hwnd, centerX, centerY, 1, net.aibote.sdk.options.Mode.front, null);
            System.out.println("已点击窗口");
            
            // 5. 输入文本
            winBot.sendKeys("Hello from Aibote4J Task Engine!");
            System.out.println("已输入文本");
        }
        
        System.out.println("记事本自动化任务执行完成");
    }
}