package net.aibote.utils;

import org.apache.commons.codec.binary.Base64;

public class Base64Utils {

    /**
     * * BASE64解码
     *
     * @param base64
     * @return
     */

    public static byte[] decodeBASE64(String base64) {
        return Base64.decodeBase64(base64.getBytes());

    }


    /**
     * * BASE64解码
     *
     * @param base64
     * @return
     */

    public static String decodeBASE64AsString(String base64) {
        byte[] bytes = decodeBASE64(base64);
        return new String(bytes);

    }


    /**
     * * BASE64编码
     *
     * @param bytes
     * @return
     */

    public static String encodeBASE64(byte[] bytes) {
        byte[] base64Bytes = Base64.encodeBase64(bytes);
        return new String(base64Bytes);

    }


    /**
     * * BASE64编码
     *
     * @param str
     * @return
     */

    public static String encodeBASE64(String str) {
        return encodeBASE64(str.getBytes());

    }

}
