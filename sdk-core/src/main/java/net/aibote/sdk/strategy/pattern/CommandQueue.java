package net.aibote.sdk.strategy.pattern;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 命令队列 - 批量执行命令
 */
@Slf4j
public class CommandQueue {

    private final BlockingQueue<Command> queue;
    private final ExecutorService executorService;
    private final CommandHistory history;
    private volatile boolean running = false;

    public CommandQueue(int queueSize) {
        this.queue = new LinkedBlockingQueue<>(queueSize);
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "CommandQueueExecutor");
            t.setDaemon(false);
            return t;
        });
        this.history = new CommandHistory();
    }

    /**
     * 启动队列处理
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;
        executorService.execute(() -> {
            while (running) {
                try {
                    Command command = queue.take();
                    history.execute(command);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("命令队列执行出错", e);
                }
            }
        });

        log.info("命令队列已启动");
    }

    /**
     * 停止队列处理
     */
    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("命令队列已停止");
    }

    /**
     * 添加命令到队列
     */
    public void enqueue(Command command) throws InterruptedException {
        if (!running) {
            throw new IllegalStateException("命令队列未启动");
        }
        queue.put(command);
    }

    /**
     * 获取队列大小
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * 获取历史记录
     */
    public CommandHistory getHistory() {
        return history;
    }
}

