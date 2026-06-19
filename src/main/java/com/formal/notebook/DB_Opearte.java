package com.formal.notebook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DB_Opearte
{
    // 1. 数据库连接字符串（URL）
    private static String URL;
    // 2. 你的本地 MySQL 用户名，默认一般是 root
    private static String USER;

    // 数据库密码
    private static String PASSWORD;

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

    //修改笔记本名
    public static void update_notebook_name(int notebook_id, String new_name) throws SQLException{
        String update_sql = "UPDATE notebook SET name = ? WHERE id = ?;";
        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(update_sql)){
                stmt.setString(1,new_name);
                stmt.setInt(2,notebook_id);
                stmt.executeUpdate();
                System.out.println("笔记本名已成功更新！");
            }
            catch(SQLException e){
                System.err.println("❌ 数据库修改失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        }catch(SQLException e){
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }
    }

    //查询所有笔记本
    public static List<Notebook> query_all_notebooks() throws SQLException{
        List<Notebook> notebooks = new ArrayList<>();
        String query_sql = "SELECT id, name FROM notebook;";

        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(query_sql)){
                ResultSet rt = stmt.executeQuery();
                while(rt.next()){
                    int id = rt.getInt("id");
                    String name = rt.getString("name");
                    notebooks.add(new Notebook(id, name));
                }
                return notebooks;
            }catch(SQLException e){
                System.err.println("❌ 数据库查询失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        }catch(SQLException e){
                System.err.println("❌ 数据库连接失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
        }
    }
}

