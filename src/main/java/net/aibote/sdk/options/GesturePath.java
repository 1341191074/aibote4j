package net.aibote.sdk.options;

/**
 * 手势路径
 */
public class GesturePath {
    StringBuilder gesturePathStr = new StringBuilder();

    public void addXY(int x, int y) {
        gesturePathStr.append(x).append("/");
        gesturePathStr.append(y).append("/");
    }

    /**
     * 返回原始数据
     *
     * @return
     */
    public String gesturePathStr() {
        return gesturePathStr.toString();
    }

    /**
     * 返回s 补位信息
     *
     * @return
     */
    public String gesturePathStr(String s) {
        gesturePathStr.append(s);
        return gesturePathStr.toString();
    }
}
