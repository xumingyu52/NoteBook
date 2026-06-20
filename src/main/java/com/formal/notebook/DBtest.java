package com.formal.notebook;

import java.util.List;

public class DBtest {

    public static void main(String[] args) {
        String testNotebookName = "测试笔记本";
        String testTitle = "测试标题";
        String testContent = "这是测试内容";

        try {
            // 1. 新建笔记本
            System.out.println("=== 新建笔记本 ===");
            DB_Opearte.create_new_notebook(testNotebookName);
            System.out.println("笔记本创建成功：" + testNotebookName);

            // 2. 查询笔记本列表
            System.out.println("\n=== 查询所有笔记本 ===");
            List<Notebook> notebooks = DB_Opearte.query_all_notebooks();
            for (Notebook nb : notebooks) {
                System.out.println("笔记本ID: " + nb.getId() + ", 名称: " + nb.getName());
            }

            // 获取新创建的笔记本ID
            int notebookId = -1;
            for (Notebook nb : notebooks) {
                if (nb.getName().equals(testNotebookName)) {
                    notebookId = nb.getId();
                    break;
                }
            }

            if (notebookId == -1) {
                System.err.println("未找到新创建的笔记本！");
                return;
            }
            System.out.println("获取到笔记本ID: " + notebookId);

            // 3. 新建标题
            // 3. 新建标题
            // 3. 新建标题
            System.out.println("\n=== 新建标题 ===");
            DB_Opearte.create_new_title(notebookId, testTitle);
            System.out.println("标题创建成功：" + testTitle);

            // 4. 更新内容
            System.out.println("\n=== 更新内容 ===");
            DB_Opearte.update_content(notebookId, testTitle, testContent);
            System.out.println("内容更新成功");

            // 5. 查询标题和内容
            System.out.println("\n=== 查询标题和内容 ===");
            Title_and_Content tac = DB_Opearte.query_title_and_content(notebookId, testTitle);
            if (tac != null) {
                System.out.println("notebook_id: " + tac.getNotebook_id());
                System.out.println("title: " + tac.getTitle());
                System.out.println("content: " + tac.getContent());
            } else {
                System.out.println("未查到该标题内容");
            }

        } catch (Exception e) {
            System.err.println("测试过程出错：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 6. 删除创建的标题（清理测试数据）
            System.out.println("\n=== 清理测试数据 ===");
            try {
                // 需要先获取笔记本ID
                List<Notebook> notebooks = DB_Opearte.query_all_notebooks();
                for (Notebook nb : notebooks) {
                    if (nb.getName().equals(testNotebookName)) {
                        DB_Opearte.delete_title(nb.getId(), testTitle);
                        System.out.println("标题已删除：" + testTitle);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("清理数据失败：" + e.getMessage());
            }
        }
    }
}
