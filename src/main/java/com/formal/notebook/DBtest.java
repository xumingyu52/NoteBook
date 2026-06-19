package com.formal.notebook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBtest {
    // 1. 数据库连接字符串（URL）
    // localhost:3306 是本地地址，notebook_db 是你刚建的数据库名
    private static final String URL = "jdbc:mysql://localhost:3306/notebook_db?useSSL=false&serverTimezone=UTC";

    // 2. 你的本地 MySQL 用户名，默认一般是 root
    private static final String USER = "root";

    // 3. 🔑 核心修改点：这里改成你之前在 Mac 上安装 MySQL 时自己设置的密码！
    private static final String PASSWORD = "Xmy12345678Abcd";

    public static void main(String[] args) {
        System.out.println("正在尝试连接数据库...");

        // 使用 try-with-resources 自动关闭连接，防止内存泄漏
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            if (conn != null) {
                System.out.println("✨ 恭喜你！Java 程序成功连上 MySQL 数据库了！ ✨");
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            e.printStackTrace(); // 打印具体的错误报错信息
        }
    }
}
