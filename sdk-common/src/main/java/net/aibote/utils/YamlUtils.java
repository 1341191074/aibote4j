package net.aibote.utils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * YAML工具类
 * 提供YAML文件的读取和解析功能
 */
public class YamlUtils {
    
    /**
     * 从输入流加载YAML
     * @param inputStream 输入流
     * @param clazz 目标类
     * @param <T> 泛型类型
     * @return 解析后的对象
     */
    public static <T> T loadAs(InputStream inputStream, Class<T> clazz) {
        Yaml yaml = new Yaml(new Constructor(clazz));
        return yaml.load(inputStream);
    }
    
    /**
     * 从文件加载YAML
     * @param filePath 文件路径
     * @param clazz 目标类
     * @param <T> 泛型类型
     * @return 解析后的对象
     * @throws Exception 读取文件异常
     */
    public static <T> T loadAsFile(String filePath, Class<T> clazz) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        Yaml yaml = new Yaml(new Constructor(clazz));
        return yaml.load(content);
    }
}