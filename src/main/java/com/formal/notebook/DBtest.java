package com.formal.notebook;


public class DBtest {

    public static void main(String[] args) {
        try {
            // 先查看所有笔记本
            var notebooks = DB_Opearte.query_all_notebooks();
            System.out.println("数据库中笔记本数量: " + notebooks.size());
            for (var nb : notebooks) {
                System.out.println("  id=" + nb.getId() + ", name=" + nb.getName());
            }

            int id = DB_Opearte.get_notebook_id("new");
            System.out.println("get_notebook_id(\"new\") = " + id);

            DB_Opearte.delete_notebook(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
