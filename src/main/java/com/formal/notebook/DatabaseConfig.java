package com.formal.notebook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DatabaseConfig {
    
    private static String URL;
    private static final Path APP_DIR;
    private static final Path CONFIG_FILE_PATH;
    private static final Path EXTERNAL_DB_PROPERTIES_PATH;
    
    static {
        String userHome = System.getProperty("user.home");
        APP_DIR = Paths.get(userHome, "Library", "Application Support", "NoteBook");
        CONFIG_FILE_PATH = APP_DIR.resolve("Config.properties");
        EXTERNAL_DB_PROPERTIES_PATH = APP_DIR.resolve("db.properties");

        try {
            Files.createDirectories(APP_DIR);
        } catch (IOException e) {
            System.err.println("❌ 无法创建应用配置目录：" + APP_DIR);
            e.printStackTrace();
        }

        Properties props = new Properties();
        try (InputStream resourceStream = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
            if (resourceStream != null) {
                props.load(resourceStream);
            }
        } catch (Exception e) {
            System.err.println("⚠️ 读取内置 db.properties 失败，使用默认 SQLite 路径。\n" + e.getMessage());
        }

        if (Files.exists(EXTERNAL_DB_PROPERTIES_PATH)) {
            try (InputStream fileStream = Files.newInputStream(EXTERNAL_DB_PROPERTIES_PATH)) {
                props.load(fileStream);
                System.out.println("✅ 使用外部 db.properties：" + EXTERNAL_DB_PROPERTIES_PATH);
            } catch (Exception e) {
                System.err.println("⚠️ 读取外部 db.properties 失败，继续使用内置配置。\n" + e.getMessage());
            }
        }

        String rawUrl = props.getProperty("db.url", "jdbc:sqlite:${user.home}/Library/Application Support/NoteBook/notebook.db");
        URL = rawUrl.replace("${user.home}", userHome);
        if (URL == null || URL.isBlank()) {
            URL = "jdbc:sqlite:" + APP_DIR.resolve("notebook.db");
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite 驱动加载失败！");
            e.printStackTrace();
        }
    }
    
    public static String getUrl() {
        return URL;
    }
    
    public static String getConfigFilePath() {
        return CONFIG_FILE_PATH.toString();
    }
    
    public static String getDbDir() {
        return APP_DIR.toString();
    }
}
