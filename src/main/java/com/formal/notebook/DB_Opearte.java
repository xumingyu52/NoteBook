package com.formal.notebook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

        }catch(IOException e){
            System.err.println("❌ 配置文件加载失败，请检查 db.properties 文件是否存在！");
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------//
    //----------------------------------------------------------------------------------//
    //笔记本操作函数区

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
            e.printStackTrace();
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
                e.printStackTrace(); // 打印具体的错误报错信息
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
    public static ArrayList<Notebook> query_all_notebooks() throws SQLException{
        ArrayList<Notebook> notebooks = new ArrayList<>();
        String query_sql = "SELECT id, name FROM notebook;";

        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(query_sql)){
                try (ResultSet rt = stmt.executeQuery()){

                    while(rt.next()){
                        int id = rt.getInt("id");
                        String name = rt.getString("name");
                        notebooks.add(new Notebook(id, name));
                    }
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

    /**
     * 返回笔记本id
     * @param notebook_name
     * @throws SQLException
     */
    public static int get_notebook_id(String notebook_name) throws SQLException{
        String query_sql = "SELECT id FROM notebook WHERE name = ?;";
        int notebook_id = 0;
        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(query_sql)){
                stmt.setString(1,notebook_name);
                try (ResultSet rt = stmt.executeQuery()){
                    if(rt.next()){
                        notebook_id = rt.getInt("id");
                        return notebook_id;
                    }
                }
            }catch(SQLException e){
                System.err.println("❌ 数据库查询失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }
        return notebook_id;
    }

    /**
     * 检查笔记本名称是否存在
     * @param notebook_name
     * @throws SQLException
     */
    public static boolean is_notebook_name_exists(String notebook_name) throws SQLException{
        String query_sql = "SELECT name FROM notebook WHERE name = ?;";
        boolean exists = false;
        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(query_sql)){
                stmt.setString(1,notebook_name);
                try (ResultSet rt = stmt.executeQuery()){
                    if(rt.next()){
                        exists = true;
                    }
                }
            }catch(SQLException e){
                System.err.println("❌ 数据库查询失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }
        return exists;
    }

    /**
     * 返回笔记本名称
     * @param notebook_id
     * @return
     * @throws SQLException
     */
    public static String get_notebook_name(int notebook_id) throws SQLException{
        String query_sql = "SELECT name FROM notebook WHERE id = ?;";
        String notebook_name = "";
        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(query_sql)){
                stmt.setInt(1,notebook_id);
                try (ResultSet rt = stmt.executeQuery()){
                    if(rt.next()){
                        notebook_name = rt.getString("name");
                        return notebook_name;
                    }
                }
            }catch(SQLException e){
                System.err.println("❌ 数据库查询失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }
        return notebook_name;
    }

    //----------------------------------------------------------------------------------//
    //----------------------------------------------------------------------------------//
    //笔记内容操作函数区
    /**
     * 查询该笔记本下的所有标题
     * @param notebook_id
     * @throws SQLException
    */
   public static ArrayList<String> query_all_titles(int notebook_id) throws SQLException{
        ArrayList<String> titles = new ArrayList<>();
        String query_sql = "SELECT title FROM Title_and_Content WHERE notebook_id = ?;";
        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(query_sql)){
                stmt.setInt(1,notebook_id);
                try (ResultSet rt = stmt.executeQuery()){
                    while(rt.next()){
                        titles.add(rt.getString("title"));
                    }
                }
            }catch(SQLException e){
                System.err.println("❌ 数据库查询失败，原因如下：");
                e.printStackTrace();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            e.printStackTrace();
            throw e; // 打印具体的错误报错信息
        }
        return titles;
    }
    
    /**
     * @param notebook_id
     * @param title
     * @throws SQLException
     * 在指定笔记本下新建标题数据
     * title不允许有重复
     */
    public static void create_new_title(int notebook_id, String title) throws SQLException{
        String sql = "INSERT INTO Title_and_Content (notebook_id, title, content) VALUES (?, ?, ?);";

        // 先检查标题是否已存在
        if (is_title_exists(notebook_id, title)) {
            System.err.println("❌ 标题已存在，无法重复添加！");
            return; // 直接返回，不执行插入操作
        }

        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1,notebook_id);
                stmt.setString(2,title);
                //空字符串置入
                stmt.setString(3,"# 请输入标题");
                stmt.executeUpdate();
                System.out.println("标题已成功添加！");
            }
            catch(SQLException e){
                System.err.println("❌ 数据库操作失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }
    }

    //在指定笔记本指定标题下修改内容
    public static void update_content(int notebook_id, String title, String content) throws SQLException{
        String sql = "UPDATE Title_and_Content SET content = ? WHERE notebook_id = ? AND title = ?;";

        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, content);
                stmt.setInt(2, notebook_id);
                stmt.setString(3, title);
                stmt.executeUpdate();
                //由于改函数后期会被用来自动保存，不再在终端输出提示信息
                //System.out.println("内容已成功更新！");
            }
            catch(SQLException e){
                System.err.println("❌ 数据库操作失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }
    }

    /**
     * 修改指定笔记本下的指定笔记标题
     * @param notebook_id 笔记本id
     * @param old_title 旧标题
     * @param new_title 新标题
     * @throws SQLException
     */
    public static void update_title(int notebook_id, String old_title, String new_title) throws SQLException{
        String sql = "UPDATE Title_and_Content SET title = ? WHERE notebook_id = ? AND title = ?;";

        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, new_title);
                stmt.setInt(2, notebook_id);
                stmt.setString(3, old_title);
                stmt.executeUpdate();
                //由于改函数后期会被用来自动保存，不再在终端输出提示信息
                //System.out.println("标题已成功更新！");
            }
            catch(SQLException e){
                System.err.println("❌ 数据库操作失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }
    }
    //查询指定笔记本指定标题与内容
    public static Title_and_Content query_title_and_content(int notebook_id, String title) throws SQLException{
        String query_sql = "SELECT notebook_id, title, content FROM Title_and_Content WHERE notebook_id = ? AND title = ?;";

        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){
            try (PreparedStatement stmt = conn.prepareStatement(query_sql)){
                stmt.setInt(1,notebook_id);
                stmt.setString(2,title);
                try (ResultSet rt = stmt.executeQuery()){
                    if(rt.next()){
                        return new Title_and_Content(rt.getInt("notebook_id"), rt.getString("title"), rt.getString("content"));
                    }
                }
                return null;
            }catch(SQLException e){
                System.err.println("❌ 数据库查询失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }

    }
    /**
     * 删除指定笔记本指定标题与内容
     */
    public static void delete_title(int notebook_id, String title) throws SQLException{
        String sql = "DELETE FROM Title_and_Content WHERE notebook_id = ? AND title = ?;";
        if (!is_title_exists(notebook_id, title)) {
                System.err.println("❌ 标题不存在，无法删除！");
                return; // 直接返回，不执行删除操作
            }
        try(Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)){

            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                
                stmt.setInt(1,notebook_id);
                stmt.setString(2,title);
                stmt.executeUpdate();
                System.out.println("标题已成功删除！");
            }
            catch(SQLException e){
                System.err.println("❌ 数据库操作失败，原因如下：");
                e.printStackTrace(); // 打印具体的错误报错信息
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败，原因如下：");
            throw e; // 打印具体的错误报错信息
        }
    }
    

    /**
     * 标题查重
     */
    public static boolean is_title_exists(int notebook_id, String title) throws SQLException {
        String sql = "SELECT 1 FROM Title_and_Content WHERE notebook_id = ? AND title = ? LIMIT 1;";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, notebook_id);
            stmt.setString(2, title);
            
            try (ResultSet rs = stmt.executeQuery()) {
                // 💡 rs.next() 如果为 true，说明查到了至少一条记录，代表重名了！
                return rs.next(); 
            }
        } catch (SQLException e) {
            System.err.println("❌ 查重校验失败！");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 搜索笔记（按标题搜索）
     * @param keyword 搜索关键词
     * @return 匹配的笔记列表
     */
    public static ArrayList<Title_and_Content> searchByTitle(String keyword) throws SQLException {
        ArrayList<Title_and_Content> results = new ArrayList<>();
        String sql = "SELECT notebook_id, title, content FROM Title_and_Content WHERE title LIKE ?;";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Title_and_Content(
                        rs.getInt("notebook_id"),
                        rs.getString("title"),
                        rs.getString("content")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 按标题搜索失败！");
            e.printStackTrace();
            throw e;
        }
        
        return results;
    }

    /**
     * 搜索笔记（按内容搜索）
     * @param keyword 搜索关键词
     * @return 匹配的笔记列表
     */
    public static ArrayList<Title_and_Content> searchByContent(String keyword) throws SQLException {
        ArrayList<Title_and_Content> results = new ArrayList<>();
        String sql = "SELECT notebook_id, title, content FROM Title_and_Content WHERE content LIKE ?;";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Title_and_Content(
                        rs.getInt("notebook_id"),
                        rs.getString("title"),
                        rs.getString("content")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 按内容搜索失败！");
            e.printStackTrace();
            throw e;
        }
        
        return results;
    }

    /**
     * 搜索笔记（同时按标题和内容搜索）
     * @param keyword 搜索关键词
     * @return 匹配的笔记列表
     */
    public static ArrayList<Title_and_Content> searchByTitleAndContent(String keyword) throws SQLException {
        ArrayList<Title_and_Content> results = new ArrayList<>();
        String sql = "SELECT notebook_id, title, content FROM Title_and_Content WHERE title LIKE ? OR content LIKE ?;";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Title_and_Content(
                        rs.getInt("notebook_id"),
                        rs.getString("title"),
                        rs.getString("content")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 按标题和内容搜索失败！");
            e.printStackTrace();
            throw e;
        }
        
        return results;
    }
}