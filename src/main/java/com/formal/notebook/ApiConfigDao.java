package com.formal.notebook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ApiConfigDao {
    
    private static String URL;
    
    static {
        try {
            URL = DatabaseConfig.getUrl();
            if (URL == null || URL.isBlank()) {
                throw new IllegalStateException("数据库 URL 未配置，请检查 db.properties");
            }
        } catch (Exception e) {
            System.err.println("❌ 数据库配置加载失败，请检查 db.properties 文件是否存在！");
            e.printStackTrace();
        }
    }
    
    public static void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS api_config (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "base_url TEXT NOT NULL," +
                "model_id TEXT NOT NULL," +
                "api_key TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        
        try(Connection conn = DriverManager.getConnection(URL)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.executeUpdate();
            }
        }
    }
    
    public static void addConfig(ApiConfig config) throws SQLException {
        String sql = "INSERT INTO api_config (name, base_url, model_id, api_key) VALUES (?, ?, ?, ?);";
        
        try(Connection conn = DriverManager.getConnection(URL)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, config.getName());
                stmt.setString(2, config.getBaseUrl());
                stmt.setString(3, config.getModelId());
                stmt.setString(4, CryptoUtils.encrypt(config.getApiKey()));
                stmt.executeUpdate();
            }
        }
    }
    
    public static void updateConfig(ApiConfig config) throws SQLException {
        String sql = "UPDATE api_config SET name = ?, base_url = ?, model_id = ?, api_key = ? WHERE id = ?;";
        
        try(Connection conn = DriverManager.getConnection(URL)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, config.getName());
                stmt.setString(2, config.getBaseUrl());
                stmt.setString(3, config.getModelId());
                stmt.setString(4, CryptoUtils.encrypt(config.getApiKey()));
                stmt.setInt(5, config.getId());
                stmt.executeUpdate();
            }
        }
    }
    
    public static void deleteConfig(int id) throws SQLException {
        String sql = "DELETE FROM api_config WHERE id = ?;";
        
        try(Connection conn = DriverManager.getConnection(URL)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
        }
    }
    
    public static ApiConfig getConfig(int id) throws SQLException {
        String sql = "SELECT * FROM api_config WHERE id = ?;";
        
        try(Connection conn = DriverManager.getConnection(URL)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()){
                    if(rs.next()){
                        return new ApiConfig(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("base_url"),
                            rs.getString("model_id"),
                            CryptoUtils.decrypt(rs.getString("api_key"))
                        );
                    }
                }
            }
        }
        return null;
    }
    
    public static ArrayList<ApiConfig> getAllConfigs() throws SQLException {
        ArrayList<ApiConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM api_config ORDER BY created_at DESC;";
        
        try(Connection conn = DriverManager.getConnection(URL)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                try (ResultSet rs = stmt.executeQuery()){
                    while(rs.next()){
                        configs.add(new ApiConfig(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("base_url"),
                            rs.getString("model_id"),
                            CryptoUtils.decrypt(rs.getString("api_key"))
                        ));
                    }
                }
            }
        }
        return configs;
    }
    
    public static ApiConfig getDefaultConfig() throws SQLException {
        String sql = "SELECT * FROM api_config ORDER BY id ASC LIMIT 1;";
        
        try(Connection conn = DriverManager.getConnection(URL)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                try (ResultSet rs = stmt.executeQuery()){
                    if(rs.next()){
                        return new ApiConfig(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("base_url"),
                            rs.getString("model_id"),
                            CryptoUtils.decrypt(rs.getString("api_key"))
                        );
                    }
                }
            }
        }
        return null;
    }
    
    public static boolean hasConfig() throws SQLException {
        try {
            createTable();
        } catch (SQLException ignored) {}
        
        String sql = "SELECT COUNT(*) FROM api_config;";
        
        try(Connection conn = DriverManager.getConnection(URL)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                try (ResultSet rs = stmt.executeQuery()){
                    if(rs.next()){
                        return rs.getInt(1) > 0;
                    }
                }
            }
        }
        return false;
    }
}