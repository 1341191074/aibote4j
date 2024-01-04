package net.aibote.sdk;

import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class Aibote {
    private final Object lockObj = new Object();//创建一个
    public String runStatus;

    @Setter
    private byte[] retBuffer;

    public ChannelHandlerContext aiboteChanel;

    private long retTimeout = 2000; // 正常下获取返回值的时间。
    private long retDelayTimeout = 6000; // 超时情况获取返回值的时间。

    public Aibote() {
        this.runStatus = "未运行";
    }

    public Aibote(ChannelHandlerContext aiboteChanel) {
        this.aiboteChanel = aiboteChanel;
        this.runStatus = "未运行";
    }

    public abstract String getScriptName();

    public abstract void doScript();

    public static String getVersion() {
        return "2023-11-25";
    }

    public void sleep(int millisecondsTimeout) {
        try {
            Thread.sleep(millisecondsTimeout);
        } catch (InterruptedException e) {
        }
    }

    private void send(String... arrArgs) {
        this.send(this.retTimeout, arrArgs);
    }

    private void send(long timeOut, String... arrArgs) {
        if (this.aiboteChanel == null) {
            throw new RuntimeException("链接错误");
        }
        this.aiboteChanel.writeAndFlush(arrArgs);
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        synchronized (lockObj) {
            while (this.retBuffer == null) {
                log.info(String.valueOf( stopwatch.getTime(TimeUnit.MILLISECONDS)));
                if (stopwatch.getTime(TimeUnit.MILLISECONDS) > retTimeout) {
                    break;
                } else {
                    sleep(200);
                }
            }
        }
        stopwatch.stop();
    }

    private void sendBytes(byte[] arrArgs) {
        if (this.aiboteChanel == null) {
            throw new RuntimeException("链接错误");
        }
        this.aiboteChanel.writeAndFlush(arrArgs);
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        synchronized (lockObj) {
            while (this.retBuffer == null) {
                if (stopwatch.getTime(TimeUnit.MILLISECONDS) > retTimeout) {
                    break;
                } else {
                    sleep(200);
                }
            }
        }
        stopwatch.stop();
    }

    public byte[] bytesCmd(String... arrArgs) {
        this.send(arrArgs);
        if (this.retBuffer != null) {
            byte[] ret = this.retBuffer;
            this.retBuffer = null;
            return ret;
        }
        return null;
    }

    public boolean boolCmd(String... arrArgs) {
        this.send(arrArgs);
        if (this.retBuffer != null) {
            byte[] ret = this.retBuffer;
            this.retBuffer = null;
            return "true".equals(new String(ret));
        }
        return false;
    }

    public boolean boolDelayCmd(String... arrArgs) {
        this.send(this.retDelayTimeout, arrArgs);
        if (this.retBuffer != null) {
            byte[] ret = this.retBuffer;
            this.retBuffer = null;
            return "true".equals(new String(ret));
        }
        return false;
    }

    protected String strCmd(String... arrArgs) {
        this.send(arrArgs);
        if (this.retBuffer != null) {
            byte[] ret = this.retBuffer;
            this.retBuffer = null;
            String retStr = new String(ret);
            if (!"null".equals(retStr)) {
                return retStr;
            }
        }
        return null;
    }

    protected String strDelayCmd(String... arrArgs) {
        this.send(this.retDelayTimeout, arrArgs);
        if (this.retBuffer != null) {
            byte[] ret = this.retBuffer;
            this.retBuffer = null;
            String retStr = new String(ret);
            if (!"null".equals(retStr)) {
                return retStr;
            }
        }
        return null;
    }

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
            this.sendBytes(byteArrayOutputStream.toByteArray());
            if (this.retBuffer != null) {
                byte[] ret = this.retBuffer;
                this.retBuffer = null;
                return "true".equals(new String(ret));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }
}
