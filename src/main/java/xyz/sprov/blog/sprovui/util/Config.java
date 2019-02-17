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
            properties.setProperty("port", "80");
            properties.setProperty("username", "sprov");
            properties.setProperty("password", "blog.sprov.xyz");
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
        return Integer.parseInt(properties.getProperty("port", "80"));
    }

    public static String getUsername() {
        return properties.getProperty("username", "sprov");
    }

    public static String getPassword() {
        return properties.getProperty("password", "blog.sprov.xyz");
    }

}
