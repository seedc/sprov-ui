package xyz.sprov.blog.sprovui.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static final String configPath = "/etc/sprov-ui/sprov-ui.conf";
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(new FileReader(configPath));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            properties.setProperty("port", "80"); // 监听端口
            properties.setProperty("username", "sprov"); // 用户名
            properties.setProperty("password", "blog.sprov.xyz"); // 密码
            properties.setProperty("basePath", ""); // contextPath，静态文件问题尚未解决，暂时无用
            properties.setProperty("keystoreFile", ""); // jks 证书路径
            properties.setProperty("keystorePass", ""); // jks 证书密码
            properties.setProperty("maxWrongPassCount", String.valueOf(5)); // 密码错误最大次数
            properties.setProperty("loginTitle", "sprov-ui 登录"); // 登录页面标题
            properties.setProperty("loginFooter", "Github - <a href=\"https://github.com/sprov065/sprov-ui\" target=\"_blank\">sprov-ui</a>"); // 登录页面 Foot
            try {
                File file = new File(configPath);
                FileUtils.forceMkdir(file.getParentFile());
                properties.store(new FileOutputStream(file), "sprov-ui config");
            } catch (Exception e1) {
                System.err.println(e1.getMessage());
            }
        }
    }

    private Config() {}

    public static int getPort() {
        try {
            return Integer.parseInt(properties.getProperty("port", "80"));
        } catch (Exception ignore) {
            properties.setProperty("port", String.valueOf(80));
            return 80;
        }
    }

    public static String username() {
        return properties.getProperty("username", "sprov");
    }

    public static String password() {
        return properties.getProperty("password", "blog.sprov.xyz");
    }

    public static void setBasePath(String basePath) {
        properties.setProperty("basePath", basePath);
    }

    public static String basePath() {
//        return properties.getProperty("basePath", "");
        return "";
    }

    public static void setApiPort(int port) {
        properties.setProperty("apiPort", String.valueOf(port));
    }

    public static int apiPort() {
        return Integer.parseInt(properties.getProperty("apiPort", "-1"));
    }

    public static String keystoreFile() {
        return properties.getProperty("keystoreFile", "");
    }

    public static String keystorePass() {
        return properties.getProperty("keystorePass", null);
    }

    public static int maxWrongPassCount() {
        try {
            return Integer.parseInt(properties.getProperty("maxWrongPassCount", String.valueOf(5)));
        } catch (Exception ignore) {
            properties.setProperty("maxWrongPassCount", String.valueOf(5));
            return 5;
        }
    }

    public static String loginTitle() {
        return properties.getProperty("loginTitle", "sprov-ui 登录");
    }

    public static String loginFooter() {
        return properties.getProperty("loginFooter", "Github - <a href=\"https://github.com/sprov065/sprov-ui\" target=\"_blank\">sprov-ui</a>");
    }

    public static String currentVersion() {
        return "3.1.0";
    }

}
