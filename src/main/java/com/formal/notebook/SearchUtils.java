package com.formal.notebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchUtils {

    private static final int CONTEXT_LENGTH = 100; // 上下文片段长度
    private static final int MAX_FRAGMENTS = 3;    // 最多显示的上下文片段数

    /**
     * 提取包含关键词的上下文片段
     * @param content 原始内容
     * @param keyword 搜索关键词
     * @return 上下文片段列表
     */
    public static List<String> extractContextFragments(String content, String keyword) {
        List<String> fragments = new ArrayList<>();
        if (content == null || content.isEmpty() || keyword == null || keyword.isEmpty()) {
            return fragments;
        }

        String lowerContent = content.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int keywordLength = keyword.length();
        int halfContext = CONTEXT_LENGTH / 2;

        int index = 0;
        while ((index = lowerContent.indexOf(lowerKeyword, index)) != -1) {
            int start = Math.max(0, index - halfContext);
            int end = Math.min(content.length(), index + keywordLength + halfContext);
            
            String fragment = content.substring(start, end);
            
            // 添加省略号
            if (start > 0) {
                fragment = "..." + fragment;
            }
            if (end < content.length()) {
                fragment = fragment + "...";
            }
            
            fragments.add(fragment);
            
            // 移动到下一个位置继续搜索
            index += keywordLength;
            
            // 限制片段数量
            if (fragments.size() >= MAX_FRAGMENTS) {
                break;
            }
        }

        return fragments;
    }

    /**
     * 高亮关键词
     * @param text 原始文本
     * @param keyword 关键词
     * @return 高亮后的HTML文本
     */
    public static String highlightKeyword(String text, String keyword) {
        if (text == null || text.isEmpty() || keyword == null || keyword.isEmpty()) {
            return text;
        }

        // 使用正则表达式进行不区分大小写的替换
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        
        // 使用HTML标签高亮关键词
        return matcher.replaceAll("<mark>$0</mark>");
    }

    /**
     * 计算相关性评分
     * @param title 标题
     * @param content 内容
     * @param keyword 关键词
     * @return 相关性评分（分数越高越相关）
     */
    public static int calculateRelevanceScore(String title, String content, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return 0;
        }

        String lowerTitle = title != null ? title.toLowerCase() : "";
        String lowerContent = content != null ? content.toLowerCase() : "";
        String lowerKeyword = keyword.toLowerCase();

        int score = 0;

        // 标题匹配权重更高
        int titleMatches = countOccurrences(lowerTitle, lowerKeyword);
        score += titleMatches * 10;

        // 内容匹配
        int contentMatches = countOccurrences(lowerContent, lowerKeyword);
        score += contentMatches * 1;

        // 如果标题完全匹配关键词，给予额外加分
        if (lowerTitle.equals(lowerKeyword)) {
            score += 50;
        }

        // 如果标题以关键词开头，给予加分
        if (lowerTitle.startsWith(lowerKeyword)) {
            score += 20;
        }

        return score;
    }

    /**
     * 计算字符串中某个子串的出现次数
     * @param text 文本
     * @param substring 子串
     * @return 出现次数
     */
    private static int countOccurrences(String text, String substring) {
        if (text == null || text.isEmpty() || substring == null || substring.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        
        return count;
    }

    /**
     * 对搜索结果按相关性排序
     * @param results 搜索结果列表
     */
    public static void sortByRelevance(List<SearchResult> results) {
        Collections.sort(results, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult r1, SearchResult r2) {
                // 按相关性评分降序排序
                return Integer.compare(r2.getRelevanceScore(), r1.getRelevanceScore());
            }
        });
    }

    /**
     * 分页处理搜索结果
     * @param allResults 所有搜索结果
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 当前页的搜索结果
     */
    public static List<SearchResult> paginateResults(List<SearchResult> allResults, int page, int pageSize) {
        if (allResults == null || allResults.isEmpty()) {
            return new ArrayList<>();
        }

        int startIndex = (page - 1) * pageSize;
        if (startIndex >= allResults.size()) {
            return new ArrayList<>();
        }

        int endIndex = Math.min(startIndex + pageSize, allResults.size());
        return new ArrayList<>(allResults.subList(startIndex, endIndex));
    }

    /**
     * 获取总页数
     * @param totalItems 总项目数
     * @param pageSize 每页大小
     * @return 总页数
     */
    public static int getTotalPages(int totalItems, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    /**
     * 格式化搜索结果显示
     * @param result 搜索结果
     * @param notebookName 笔记本名称
     * @return 格式化的显示字符串
     */
    public static String formatSearchResult(SearchResult result, String notebookName) {
        StringBuilder sb = new StringBuilder();
        
        // 添加笔记本和标题
        sb.append("📒 ").append(notebookName).append(" / ").append(result.getTitle()).append("\n");
        
        // 添加相关性评分（调试用，可以移除）
        // sb.append("  相关性: ").append(result.getRelevanceScore()).append("\n");
        
        // 添加上下文片段
        if (result.getContextFragment() != null && !result.getContextFragment().isEmpty()) {
            sb.append("  ").append(result.getContextFragment());
        }
        
        return sb.toString();
    }
}