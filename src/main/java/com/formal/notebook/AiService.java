package com.formal.notebook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AiService {
    
    private ApiConfig config;
    
    public AiService(ApiConfig config) {
        this.config = config;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
    
    public String chat(String prompt, String context) throws Exception {
        String urlStr = config.getBaseUrl();
        if (!urlStr.endsWith("/")) {
            urlStr += "/";
        }
        urlStr += "chat/completions";
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
        conn.setDoOutput(true);
        
        String systemPrompt = "你是一个专业的笔记助手。请根据用户提供的笔记内容和指令，帮助用户撰写、修改或完善笔记。回复格式为纯文本，不要包含Markdown代码块标记。";
        String userContent = "笔记内容：\n" + (context == null ? "" : context) + "\n\n指令：\n" + prompt;
        String jsonPayload = String.format(
            "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}]}",
            escapeJson(config.getModelId()),
            escapeJson(systemPrompt),
            escapeJson(userContent)
        );
        
        try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
            wr.write(jsonPayload);
            wr.flush();
        }
        
        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            StringBuilder error = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    error.append(line);
                }
            }
            throw new Exception("API请求失败，状态码: " + responseCode + ", 错误信息: " + error.toString());
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        
        conn.disconnect();
        
        String jsonResponse = response.toString();
        int contentStart = jsonResponse.indexOf("\"content\":\"") + 11;
        int contentEnd = jsonResponse.indexOf("\"", contentStart);
        
        if (contentStart > 10 && contentEnd > contentStart) {
            return jsonResponse.substring(contentStart, contentEnd).replace("\\n", "\n").replace("\\t", "\t");
        }
        
        return jsonResponse;
    }
}