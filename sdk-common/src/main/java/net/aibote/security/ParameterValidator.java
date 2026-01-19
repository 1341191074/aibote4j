package net.aibote.security;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 参数验证工具类 - 提供统一的参数验证功能
 *
 * 特点:
 * - 防止SQL注入、XSS攻击
 * - 参数类型和范围检验
 * - 详细的验证错误信息
 */
@Slf4j
public class ParameterValidator {

    // 正则表达式模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            ".*(;|'|\\*|--|/\\*|\\*/|xp_|sp_|exec|execute|select|insert|update|delete|drop|create|alter|script|javascript).*",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern XSS_PATTERN = Pattern.compile(
            ".*(\\<|\\>|script|iframe|onerror|onload|javascript:|vbscript:).*",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    /**
     * 验证字符串不为空
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
    }

    /**
     * 验证字符串长度
     */
    public static void validateLength(String value, int minLength, int maxLength, String fieldName) {
        validateNotEmpty(value, fieldName);
        int len = value.length();
        if (len < minLength || len > maxLength) {
            throw new IllegalArgumentException(
                    String.format("%s 长度必须在 %d 到 %d 之间，当前长度: %d",
                            fieldName, minLength, maxLength, len));
        }
    }

    /**
     * 验证整数范围
     */
    public static void validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    String.format("%s 必须在 %d 到 %d 之间，当前值: %d",
                            fieldName, min, max, value));
        }
    }

    /**
     * 验证长整数范围
     */
    public static void validateRange(long value, long min, long max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    String.format("%s 必须在 %d 到 %d 之间，当前值: %d",
                            fieldName, min, max, value));
        }
    }

    /**
     * 防止SQL注入
     */
    public static void validateSQLSafety(String value, String fieldName) {
        validateNotEmpty(value, fieldName);
        if (SQL_INJECTION_PATTERN.matcher(value).matches()) {
            throw new SecurityException(fieldName + " 包含非法字符，可能是SQL注入攻击");
        }
    }

    /**
     * 防止XSS攻击
     */
    public static void validateXSSSafety(String value, String fieldName) {
        validateNotEmpty(value, fieldName);
        if (XSS_PATTERN.matcher(value).matches()) {
            throw new SecurityException(fieldName + " 包含非法字符，可能是XSS攻击");
        }
    }

    /**
     * 验证邮箱格式
     */
    public static void validateEmail(String email, String fieldName) {
        validateNotEmpty(email, fieldName);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(fieldName + " 邮箱格式不正确");
        }
    }

    /**
     * 验证IP地址格式
     */
    public static void validateIP(String ip, String fieldName) {
        validateNotEmpty(ip, fieldName);
        if (!IP_PATTERN.matcher(ip).matches()) {
            throw new IllegalArgumentException(fieldName + " IP地址格式不正确");
        }
    }

    /**
     * 验证端口号
     */
    public static void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    String.format("端口号必须在 1 到 65535 之间，当前值: %d", port));
        }
    }

    /**
     * 验证对象不为空
     */
    public static void validateNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
    }

    /**
     * 验证布尔值
     */
    public static void validateBoolean(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 全面安全验证
     */
    public static void validateComprehensive(String value, String fieldName,
                                            int minLength, int maxLength) {
        validateNotEmpty(value, fieldName);
        validateLength(value, minLength, maxLength, fieldName);
        validateSQLSafety(value, fieldName);
        validateXSSSafety(value, fieldName);
    }
}

