package net.aibote.sdk.options;

/**
 * 用户winbot ， 标识前台执行或者后台执行
 */
public enum Mode {

    /**
     * 前台执行
     */
    front(false),
    /**
     * 后台执行
     */
    backed(true);

    private boolean boolValue;

    Mode(boolean boolValue) {
        this.boolValue = boolValue;
    }

    public boolean boolValue() {
        return boolValue;
    }

    public String boolValueStr() {
        return Boolean.toString(boolValue);
    }
}
