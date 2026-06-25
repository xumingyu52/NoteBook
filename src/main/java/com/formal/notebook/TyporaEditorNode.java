package com.formal.notebook;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class TyporaEditorNode extends StackPane {

    private final WebView webView;
    private final WebEngine webEngine;
    private String currentMarkdown = "";
    private int currentNotebookId = -1;
    private String currentTitle = "";
    private Timer saveTimer;
    private String pendingSaveContent = null;
    private boolean initialized = false;
    private String pendingInitialMarkdown = null;

    public TyporaEditorNode(String initialMarkdown) {
        this.currentMarkdown = initialMarkdown;
        this.webView = new WebView();
        this.webEngine = webView.getEngine();

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", new JavaBridge());
                initialized = true;
                if (pendingInitialMarkdown != null && !pendingInitialMarkdown.isEmpty()) {
                    webEngine.executeScript("window.setMarkdown(`" + escapeJsString(pendingInitialMarkdown) + "`)");
                    pendingInitialMarkdown = null;
                }
            }
        });

        String html = buildEditorHtml();
        webEngine.loadContent(html, "text/html");
        this.getChildren().add(webView);
    }

    private String buildEditorHtml() {
        String css = readResource("/dist/index.css");
        String js = readResource("/dist/index.min.js");
        String i18n = readResource("/dist/js/i18n/zh_CN.js");
        String icons = readResource("/dist/js/icons/material.js");
        
        return "<!DOCTYPE html>" +
            "<html lang=\"zh-CN\">" +
            "<head>" +
            "<meta charset=\"utf-8\">" +
            "<style>" + css + "</style>" +
            "<style>" +
            "body, html, #vditor { height: 100%; margin: 0; padding: 0; border: none; }" +
            "body { font-family: \"SimSun\", \"STSong\", \"宋体\", serif !important; }" +
            ".vditor-wysiwyg, .vditor-wysiwyg .vditor-reset, .vditor-wysiwyg p, .vditor-wysiwyg span {" +
            "    font-family: \"SimSun\", \"STSong\", \"宋体\", serif !important;" +
            "    -webkit-font-smoothing: antialiased;" +
            "    -moz-osx-font-smoothing: grayscale;" +
            "}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div id=\"vditor\"></div>" +
            "<script>" + i18n + "</script>" +
            "<script>" + icons + "</script>" +
            "<script>" + js + "</script>" +
            "<script>" +
            "var vditor;" +
            "document.addEventListener('DOMContentLoaded', function() {" +
            "    try {" +
            "        if (typeof Vditor === 'undefined') {" +
            "            document.getElementById('vditor').innerHTML = 'Vditor 未加载'; return;" +
            "        }" +
            "        vditor = new Vditor('vditor', {" +
            "            mode: 'wysiwyg'," +
            "            value: ''," +
            "            cache: { enable: false }," +
            "            toolbarConfig: { hide: false }," +
            "            icon: 'material'," +
            "            after: function() {" +
            "                if (window.javaBridge) window.javaBridge.onReady();" +
            "            }," +
            "            input: function(value) {" +
            "                if (window.javaBridge) window.javaBridge.updateContent(value);" +
            "            }" +
            "        });" +
            "    } catch(e) {" +
            "        document.getElementById('vditor').innerHTML = '初始化失败: ' + e.message;" +
            "    }" +
            "});" +
            "window.setMarkdown = function(md) { if(vditor) vditor.setValue(md); };" +
            "window.getMarkdown = function() { return vditor ? vditor.getValue() : ''; };" +
            "</script>" +
            "</body>" +
            "</html>";
    }

    private String readResource(String path) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            return "/* Failed to load " + path + ": " + e.getMessage() + " */";
        }
        return sb.toString();
    }

    public void setNoteInfo(int notebookId, String title) {
        this.currentNotebookId = notebookId;
        this.currentTitle = title;
    }

    public String getMarkdown() {
        return currentMarkdown;
    }

    public void setMarkdown(String markdown) {
        this.currentMarkdown = markdown;
        if (initialized) {
            webEngine.executeScript("window.setMarkdown(`" + escapeJsString(markdown) + "`)");
        } else {
            pendingInitialMarkdown = markdown;
        }
    }

    private String escapeJsString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$");
    }

    private void triggerSave(String content) {
        this.pendingSaveContent = content;
        if (saveTimer != null) saveTimer.cancel();
        saveTimer = new Timer();
        saveTimer.schedule(new TimerTask() {
            @Override
            public void run() { performSave(); }
        }, 2000);
    }

    private void performSave() {
        if (pendingSaveContent == null || currentNotebookId <= 0 || currentTitle.isEmpty()) return;
        try {
            DB_Opearte.update_content(currentNotebookId, currentTitle, pendingSaveContent);
        } catch (Exception e) { e.printStackTrace(); }
        pendingSaveContent = null;
    }

    public void saveNow() {
        if (saveTimer != null) { saveTimer.cancel(); saveTimer = null; }
        if (initialized && webEngine != null) {
            try {
                Object result = webEngine.executeScript("window.getMarkdown()");
                if (result instanceof String) pendingSaveContent = (String) result;
            } catch (Exception e) {
                if (currentMarkdown != null && !currentMarkdown.isEmpty()) pendingSaveContent = currentMarkdown;
            }
        } else if (currentMarkdown != null && !currentMarkdown.isEmpty()) {
            pendingSaveContent = currentMarkdown;
        }
        performSave();
    }

    public void shutdown() {
        saveNow();
        webEngine.load("about:blank");
    }

    public class JavaBridge {
        public void updateContent(String text) {
            currentMarkdown = text;
            triggerSave(text);
        }
        public void onReady() {}
    }
}
