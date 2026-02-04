package net.aibote.sdk;

import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import net.aibote.utils.config.ConfigManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 平台机器人抽象基类
 * 统一管理各平台共有的基础功能和通信逻辑
 */
@Slf4j
public abstract class AbstractPlatformBot {
    private final ReentrantLock lock = new ReentrantLock();
    public String runStatus;

    // 使用 AtomicReference 替代直接的 byte[] 字段，提高线程安全性
    private final AtomicReference<byte[]> retBufferRef = new AtomicReference<>();

    public ChannelHandlerContext aiboteChanel;

    // 使用配置管理器获取超时配置
    private long retTimeout; // 正常下获取返回值的时间。
    private long retDelayTimeout; // 超时情况获取返回值的时间。

    public AbstractPlatformBot() {
        this.runStatus = "未运行";
        initializeTimeouts();
    }

    public AbstractPlatformBot(ChannelHandlerContext aiboteChanel) {
        this.aiboteChanel = aiboteChanel;
        this.runStatus = "未运行";
        initializeTimeouts();
    }
    
    /**
     * 初始化超时配置
     */
    private void initializeTimeouts() {
        this.retTimeout = ConfigManager.getInstance().getCommunicationConfig().getResponseTimeout();
        this.retDelayTimeout = ConfigManager.getInstance().getCommunicationConfig().getDelayResponseTimeout();
    }

    // 注：getScriptName() 和 doScript() 方法已被弃用
    // 现代任务执行使用 TaskDefinition 接口替代

    /**
     * 获取框架版本
     * @return 版本号
     */
    public static String getVersion() {
        return "2023-11-25";
    }

    /**
     * 线程休眠
     * @param millisecondsTimeout 休眠时间（毫秒）
     */
    public void sleep(int millisecondsTimeout) {
        try {
            Thread.sleep(millisecondsTimeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
        }
    }

    /**
     * 发送命令
     * @param arrArgs 命令参数
     */
    protected void send(String... arrArgs) {
        this.send(this.retTimeout, arrArgs);
    }
    
    /**
     * 安全地设置响应缓冲区
     * @param buffer 响应数据
     */
    public void setRetBuffer(byte[] buffer) {
        retBufferRef.set(buffer);
    }
    
    /**
     * 安全地获取并清空响应缓冲区
     * @return 响应数据，如果为空则返回null
     */
    private byte[] takeRetBuffer() {
        return retBufferRef.getAndSet(null);
    }

    /**
     * 设置发送数据格式
     * 数据格式: len/len/len\ndata
     * @param arrArgs 命令参数
     * @return 格式化后的数据字符串
     */
    protected String setSendData(String... arrArgs) {
        //数据格式 len/len/len\ndata
        StringBuilder strData = new StringBuilder();
        StringBuilder tempStr = new StringBuilder();
        for (String args : arrArgs) {
            if (args == null) args = "";
            tempStr.append(args);
            strData.append(args.getBytes(StandardCharsets.UTF_8).length);//获取包含中文实际长度
            strData.append('/');
        }
        strData.append('\n');
        strData.append(tempStr);
        return strData.toString();
    }

    /**
     * 发送协议到driver
     * @param strData 格式化后的数据
     * @return 响应字符串
     */
    protected String sendData(String strData) {
        byte[] bytes = this.sendDataForBytes(strData);
        if (null == bytes) {
            return null;
        }
        return new String(bytes);
    }

    /**
     * 发送协议到driver并返回字节数据
     * 主要用于返回图片使用
     * @param strData 格式化后的数据
     * @return 响应字节数组
     */
    protected byte[] sendDataForBytes(String strData) {
        log.info("发送命令：" + strData);
        // 直接发送数据并等待响应
        if (this.aiboteChanel == null) {
            throw new RuntimeException("链接错误");
        }
        
        this.aiboteChanel.writeAndFlush(strData.getBytes(StandardCharsets.UTF_8));
        
        // 使用 CompletableFuture 实现非阻塞等待
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        
        // 在单独的线程中处理超时
        Thread timeoutThread = new Thread(() -> {
            try {
                Thread.sleep(retTimeout);
                if (!future.isDone()) {
                    future.completeExceptionally(new TimeoutException("命令执行超时"));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            }
        });
        timeoutThread.setDaemon(true);
        timeoutThread.start();
        
        // 轮询检查响应（减少锁竞争）
        long startTime = System.currentTimeMillis();
        while (!future.isDone() && (System.currentTimeMillis() - startTime) < retTimeout) {
            byte[] buffer = takeRetBuffer();
            if (buffer != null) {
                future.complete(buffer);
                break;
            }
            
            // 短暂休眠避免过度占用CPU
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
                break;
            }
        }
        
        try {
            return future.get(0, TimeUnit.MILLISECONDS); // 立即返回结果或异常
        } catch (Exception e) {
            log.error("发送数据失败", e);
            return null;
        }
    }

    /**
     * 发送命令
     * @param timeOut 超时时间
     * @param arrArgs 命令参数
     */
    protected void send(long timeOut, String... arrArgs) {
        if (this.aiboteChanel == null) {
            throw new RuntimeException("链接错误");
        }
        
        // 使用新的协议格式
        this.aiboteChanel.writeAndFlush(arrArgs);
        
        CountDownLatch latch = new CountDownLatch(1);
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        
        try {
            // 使用配置管理器获取重试参数
            int retryTimes = ConfigManager.getInstance().getCommunicationConfig().getRetryTimes();
            int retryInterval = ConfigManager.getInstance().getCommunicationConfig().getRetryInterval();
            
            long remainingTime = timeOut;
            long startTime = System.currentTimeMillis();
            
            for (int attempt = 0; attempt <= retryTimes && remainingTime > 0; attempt++) {
                if (attempt > 0) {
                    log.debug("第{}次尝试获取响应", attempt);
                    Thread.sleep(retryInterval);
                }
                
                while (remainingTime > 0) {
                    byte[] buffer = takeRetBuffer();
                    if (buffer != null) {
                        return; // 成功获取到响应
                    }
                    
                    // 短暂休眠避免过度占用CPU
                    long waitTime = Math.min(5, remainingTime);
                    Thread.sleep(Math.min(waitTime, 5));
                    
                    remainingTime = timeOut - (System.currentTimeMillis() - startTime);
                    
                    // 检查是否超时
                    if (remainingTime <= 0) {
                        break;
                    }
                }
            }
            
            // 超时处理
            log.warn("命令执行超时: " + String.join(",", arrArgs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("发送命令被中断", e);
        } finally {
            stopwatch.stop();
        }
    }

    /**
     * 发送字节命令
     * @param arrArgs 命令参数
     * @return 响应字节数组
     */
    protected byte[] sendBytes(byte[] arrArgs) {
        if (this.aiboteChanel == null) {
            throw new RuntimeException("链接错误");
        }
        
        CountDownLatch latch = new CountDownLatch(1);
        
        this.aiboteChanel.writeAndFlush(arrArgs);
        
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        
        try {
            long remainingTime = retTimeout;
            long startTime = System.currentTimeMillis();
            
            while (remainingTime > 0) {
                byte[] buffer = takeRetBuffer();
                if (buffer != null) {
                    return buffer; // 成功获取到响应
                }
                
                // 短暂休眠避免过度占用CPU
                long waitTime = Math.min(5, remainingTime);
                Thread.sleep(Math.min(waitTime, 5));
                
                remainingTime = retTimeout - (System.currentTimeMillis() - startTime);
            }
            
            // 超时处理
            log.warn("字节命令执行超时");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("发送字节命令被中断", e);
        } finally {
            stopwatch.stop();
        }
        return null;
    }

    /**
     * 执行字节命令
     * @param arrArgs 命令参数
     * @return 响应字节数组
     */
    public byte[] bytesCmd(String... arrArgs) {
        // 清空之前的响应
        setRetBuffer(null);
        
        this.send(arrArgs);
        
        byte[] buffer = takeRetBuffer();
        if (buffer != null) {
            return buffer;
        }
        return null;
    }

    /**
     * 执行布尔命令
     * @param arrArgs 命令参数
     * @return 响应布尔值
     */
    public boolean boolCmd(String... arrArgs) {
        // 清空之前的响应
        setRetBuffer(null);
        
        this.send(arrArgs);
        
        byte[] buffer = takeRetBuffer();
        if (buffer != null) {
            return "true".equals(new String(buffer));
        }
        return false;
    }

    /**
     * 执行延迟布尔命令
     * @param arrArgs 命令参数
     * @return 响应布尔值
     */
    protected boolean boolDelayCmd(String... arrArgs) {
        // 清空之前的响应
        setRetBuffer(null);
        
        this.send(this.retDelayTimeout, arrArgs);
        
        byte[] buffer = takeRetBuffer();
        if (buffer != null) {
            return "true".equals(new String(buffer));
        }
        return false;
    }

    /**
     * 执行字符串命令
     * @param arrArgs 命令参数
     * @return 响应字符串
     */
    protected String strCmd(String... arrArgs) {
        // 清空之前的响应
        setRetBuffer(null);
        
        this.send(arrArgs);
        
        byte[] buffer = takeRetBuffer();
        if (buffer != null) {
            String retStr = new String(buffer);
            if (!"null".equals(retStr)) {
                return retStr;
            }
        }
        return null;
    }

    /**
     * 执行延迟字符串命令
     * @param arrArgs 命令参数
     * @return 响应字符串
     */
    protected String strDelayCmd(String... arrArgs) {
        // 清空之前的响应
        setRetBuffer(null);
        
        this.send(this.retDelayTimeout, arrArgs);
        
        byte[] buffer = takeRetBuffer();
        if (buffer != null) {
            String retStr = new String(buffer);
            if (!"null".equals(retStr)) {
                return retStr;
            }
        }
        return null;
    }

    /**
     * 发送文件
     * @param functionName 函数名
     * @param androidFilePath Android文件路径
     * @param fileData 文件数据
     * @return 是否发送成功
     */
    protected boolean sendFile(String functionName, String androidFilePath, byte[] fileData) {
        StringBuilder strData = new StringBuilder();
        strData.append(functionName.getBytes().length).append("/");
        strData.append(androidFilePath.getBytes().length).append("/");
        strData.append(fileData.length).append("/");
        strData.append(functionName);
        strData.append(androidFilePath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(strData.toString().getBytes(StandardCharsets.UTF_8));
            byteArrayOutputStream.write(fileData);
            
            // 清空之前的响应
            setRetBuffer(null);
            
            this.sendBytes(byteArrayOutputStream.toByteArray());
            
            byte[] buffer = takeRetBuffer();
            if (buffer != null) {
                return "true".equals(new String(buffer));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }
}