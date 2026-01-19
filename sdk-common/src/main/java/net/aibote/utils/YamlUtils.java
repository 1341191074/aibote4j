package net.aibote.utils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * YAML工具类
 * 提供YAML文件的读取和解析功能
 * 支持安全的YAML加载，防止代码执行漏洞
 */
public class YamlUtils {
    
    private static final LoaderOptions DEFAULT_LOADER_OPTIONS = createSecureLoaderOptions();

    /**
     * 创建安全的加载配置
     * @return 配置对象
     */
    private static LoaderOptions createSecureLoaderOptions() {
        LoaderOptions options = new LoaderOptions();
        options.setMaxAliasesForCollections(50);
        return options;
    }

    /**
     * 从输入流加载YAML
     * @param inputStream 输入流
     * @param clazz 目标类
     * @param <T> 泛型类型
     * @return 解析后的对象
     */
    public static <T> T loadAs(InputStream inputStream, Class<T> clazz) {
        Constructor constructor = new Constructor(clazz, DEFAULT_LOADER_OPTIONS);
        Yaml yaml = new Yaml(constructor);
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
        String content = Files.readString(Paths.get(filePath));
        Constructor constructor = new Constructor(clazz, DEFAULT_LOADER_OPTIONS);
        Yaml yaml = new Yaml(constructor);
        return yaml.load(content);
    }
}