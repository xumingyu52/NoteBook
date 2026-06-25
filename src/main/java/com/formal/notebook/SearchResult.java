package com.formal.notebook;

public class SearchResult {
    private int notebook_id;
    private String title;
    private String content;
    private String contextFragment; // 包含关键词的上下文片段
    private int relevanceScore;     // 相关性评分
    private String highlightFragment; // 高亮显示的片段

    public SearchResult(int notebook_id, String title, String content, 
                       String contextFragment, int relevanceScore, String highlightFragment) {
        this.notebook_id = notebook_id;
        this.title = title;
        this.content = content;
        this.contextFragment = contextFragment;
        this.relevanceScore = relevanceScore;
        this.highlightFragment = highlightFragment;
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

    public String getContextFragment() {
        return contextFragment;
    }

    public int getRelevanceScore() {
        return relevanceScore;
    }

    public String getHighlightFragment() {
        return highlightFragment;
    }

    // 设置方法
    public void setContextFragment(String contextFragment) {
        this.contextFragment = contextFragment;
    }

    public void setRelevanceScore(int relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public void setHighlightFragment(String highlightFragment) {
        this.highlightFragment = highlightFragment;
    }
}