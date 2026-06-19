package com.formal.notebook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DB_Opearte
{
    // 1. 数据库连接字符串（URL）
    private static final String URL = "jdbc:mysql://localhost:3306/notebook_db?useSSL=false&serverTimezone=UTC";

    // 2. 你的本地 MySQL 用户名，默认一般是 root
    private static final String USER = "root";

    // 数据库密码
    private static final String PASSWORD = "Xmy12345678Abcd";

    //增加笔记本
    public static void create_new_notebook(String notebook_name) throws SQLException{
        String sql = "INSERT INTO notebook (name) VALUES (?)";

        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                // 3. 设置参数并执行 SQL 语句
                stmt.setString(1,notebook_name);
                stmt.executeUpdate();
            }catch(SQLException e){
                System.err.println("❌ 数据库操作失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
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
