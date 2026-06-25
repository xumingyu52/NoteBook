package com.formal.notebook;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    
    private static String URL;
    private static final String DB_DIR;
    
    static {
        String userHome = System.getProperty("user.home");
        DB_DIR = userHome + File.separator + "Library" + File.separator + "Application Support" + File.separator + "NoteBook";
        
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
            if (is != null) {
                props.load(is);
                String rawUrl = props.getProperty("db.url", "jdbc:sqlite:${user.home}/Library/Application Support/NoteBook/notebook.db");
                URL = rawUrl.replace("${user.home}", userHome);
            } else {
                // 默认配置
                URL = "jdbc:sqlite:" + DB_DIR + File.separator + "notebook.db";
                System.err.println("⚠️ 未找到 db.properties，使用默认 SQLite 路径: " + URL);
            }
            
            // 确保数据库目录存在
            File dbDir = new File(DB_DIR);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            
            // 加载 SQLite 驱动
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.err.println("❌ 数据库配置加载失败！");
            e.printStackTrace();
        }
    }
    
    public static String getUrl() {
        return URL;
    }
    
    public static String getDbDir() {
        return DB_DIR;
    }
}
