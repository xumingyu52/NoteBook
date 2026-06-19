package com.formal.notebook;

public class Notebook {
    private int id;
    private String name;

    // 构造函数
    public Notebook(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "笔记本 [ID=" + id + ", 名称=" + name + "]";
    }
}
