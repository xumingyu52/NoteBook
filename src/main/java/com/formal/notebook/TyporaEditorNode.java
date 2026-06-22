package com.formal.notebook;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 开箱即用的 Typora 风格 Markdown 编辑器 Node
 * 直接 new TyporaEditorNode() 然后 add 到你的布局里即可
 */
public class TyporaEditorNode extends StackPane {

    private final WebView webView;
    private final WebEngine webEngine;
    private String currentMarkdown = "";

    public TyporaEditorNode(String initialMarkdown) {
        this.currentMarkdown = initialMarkdown;
        this.webView = new WebView();
        this.webEngine = webView.getEngine();

        // 从资源文件加载 HTML 模板
        String htmlTemplate = loadResourceFile("editor.html");
        // 替换模板变量
        String html = htmlTemplate.replace("{{INITIAL_MARKDOWN}}", escapeJsString(initialMarkdown));

        // 注入 Java 桥梁对象到 JS 环境中
        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", new JavaBridge());
            }
        });

        // 加载这段纯文本 HTML
        webEngine.loadContent(html);

        // 把 WebView 添加到当前布局
        this.getChildren().add(webView);
    }

    // 从 resources 目录加载文件内容
    private String loadResourceFile(String fileName) {
        StringBuilder content = new StringBuilder();
        try (InputStream is = getClass().getResourceAsStream("/" + fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("无法加载资源文件: " + fileName, e);
        }
        return content.toString();
    }

    // 供外部调用的：获取当前最新 Markdown 文本
    public String getMarkdown() {
        return currentMarkdown;
    }

    // 供外部调用的：从数据库读取后，塞入编辑器
    public void setMarkdown(String markdown) {
        this.currentMarkdown = markdown;
        webEngine.executeScript("window.setMarkdown(`" + escapeJsString(markdown) + "`)");
    }

    // 处理字符串中的特殊符号，防止破坏 JS 语法
    private String escapeJsString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$");
    }

    // 这个内部类就是 JS 和 Java 通信的桥梁
    public class JavaBridge {
        public void updateContent(String text) {
            currentMarkdown = text;
            // TODO: 如果你想做自动保存，可以在这里加个 Timer 触发写入数据库的逻辑
            // System.out.println("当前内容已更新: " + text);
        }

        public void onReady() {
            System.out.println("Typora 编辑器加载完毕！");
        }
    }
}