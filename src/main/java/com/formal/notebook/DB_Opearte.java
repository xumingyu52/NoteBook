package com.formal.notebook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DB_Opearte
{
    // 1. 数据库连接字符串（URL）
    private static String URL;
    // 2. 你的本地 MySQL 用户名，默认一般是 root
    private static String USER;

    // 数据库密码
    private static String PASSWORD;

    // 静态代码块，在类加载时执行，加载配置文件
    static {
        Properties props = new Properties();
        try{
            props.load(new FileInputStream("db.properties"));

            URL = props.getProperty("DB_URL");
            USER = props.getProperty("DB_USER");
            PASSWORD = props.getProperty("DB_PASSWORD");

        }catch(IOException e){
            System.err.println("❌ 配置文件加载失败，请检查 db.properties 文件是否存在！");
            e.printStackTrace();
        }
    }

    //删除笔记本
    public static void delete_notebook(int notebook_id) throws SQLException {
        String deleteContentSql = "DELETE FROM Title_and_Content WHERE notebook_id = ?;";
        String deleteNotebookSql = "DELETE FROM notebook WHERE id = ?;";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            //取消自动提交保存，防止没有完成所有删除步骤
            conn.setAutoCommit(false);

            try {
                // 1. 先删内容表里的笔记
                try (PreparedStatement stmt1 = conn.prepareStatement(deleteContentSql)) {
                    stmt1.setInt(1, notebook_id);
                    stmt1.executeUpdate();
                }

                // 2. 再删笔记本表里的记录
                try (PreparedStatement stmt2 = conn.prepareStatement(deleteNotebookSql)) {
                    stmt2.setInt(1, notebook_id);
                    stmt2.executeUpdate();
                }

                conn.commit(); // 两次都成功，才真正写入硬盘
                System.out.println("笔记本及其旗下的所有笔记已成功清空并删除！");

            } catch (SQLException e) {
                conn.rollback(); // 一步失败，全部回滚
                throw e;
            }
        }
    }

    
}
