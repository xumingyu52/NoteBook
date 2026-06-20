package com.formal.notebook;

public class Title_and_Content {
    private int notebook_id;
    private String title;
    private String content;

    // 构造函数
    public Title_and_Content(int notebook_id, String title, String content) {
        this.notebook_id = notebook_id;
        this.title = title;
        this.content = content;
    }

    // 获取方法
    public int getNotebook_id() {
        return notebook_id;
    }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
}
