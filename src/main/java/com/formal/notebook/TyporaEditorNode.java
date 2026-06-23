package com.formal.notebook;

import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 开箱即用的 Typora 风格 Markdown 编辑器 Node
 * 直接 new TyporaEditorNode() 然后 add 到你的布局里即可
 */
public class TyporaEditorNode extends StackPane {

    private final WebView webView;
    private final WebEngine webEngine;
    private String currentMarkdown = "";
    private int currentNotebookId = -1;
    private String currentTitle = "";
    private Timer saveTimer;
    private String pendingSaveContent = null;
    private boolean initialized = false;
    private String pendingInitialMarkdown = null; // 初始化完成前缓存要设置的 Markdown
    private final ImageView loadingIcon; // 加载动画图标

    public TyporaEditorNode(String initialMarkdown) {
        this.currentMarkdown = initialMarkdown;
        this.webView = new WebView();
        this.webEngine = webView.getEngine();

        // 加载旋转动画
        loadingIcon = createLoadingSpinner();
        loadingIcon.setVisible(true); // 初始显示

        // 通过 URL 加载 editor.html（保留 base URL，让相对路径的 CSS/JS 能正确解析）
        URL htmlUrl = getClass().getResource("/editor.html");
        if (htmlUrl == null) {
            throw new RuntimeException("找不到 editor.html，请确认 src/main/resources/editor.html 存在");
        }

        // 页面加载完成后：注入 Java 桥梁 + 设置初始内容 + 隐藏加载动画
        webEngine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", new JavaBridge());
                initialized = true;
                // 隐藏加载动画
                loadingIcon.setVisible(false);
                // 如果初始化完成前有缓存的 Markdown，现在设置
                if (pendingInitialMarkdown != null && !pendingInitialMarkdown.isEmpty()) {
                    webEngine.executeScript("window.setMarkdown(`" + escapeJsString(pendingInitialMarkdown) + "`)");
                    pendingInitialMarkdown = null;
                }
            }
        });

        // 用 load() 而不是 loadContent()，确保 base URL 正确
        webEngine.load(htmlUrl.toExternalForm());

        // 把 WebView 和加载动画都添加到布局（StackPane 叠加）
        this.getChildren().addAll(webView, loadingIcon);
    }

    // 创建旋转加载动画
    private ImageView createLoadingSpinner() {
        URL iconUrl = getClass().getResource("/icons/加载4_loading-four.png");
        Image img = new Image(iconUrl.toExternalForm());
        ImageView iv = new ImageView(img);
        iv.setFitWidth(48);
        iv.setFitHeight(48);

        RotateTransition rt = new RotateTransition(Duration.seconds(1.0), iv);
        rt.setByAngle(360);
        rt.setCycleCount(RotateTransition.INDEFINITE);
        rt.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rt.play();

        return iv;
    }

    // 设置当前编辑的笔记信息（用于自动保存时知道保存到哪里）
    public void setNoteInfo(int notebookId, String title) {
        this.currentNotebookId = notebookId;
        this.currentTitle = title;
        System.out.println("[DEBUG] setNoteInfo: id=" + notebookId + ", title=" + title);
    }

    // 供外部调用的：获取当前最新 Markdown 文本
    public String getMarkdown() {
        return currentMarkdown;
    }

    // 供外部调用的：从数据库读取后，塞入编辑器
    public void setMarkdown(String markdown) {
        System.out.println("[DEBUG] setMarkdown: [" + markdown + "], initialized=" + initialized);
        this.currentMarkdown = markdown;
        if (initialized) {
            webEngine.executeScript("window.setMarkdown(`" + escapeJsString(markdown) + "`)");
        } else {
            // 还没初始化完成，缓存起来等 ready 后设置
            pendingInitialMarkdown = markdown;
            System.out.println("[DEBUG] setMarkdown: cached for later (not initialized yet)");
        }
    }

    // 处理字符串中的特殊符号，防止破坏 JS 语法
    private String escapeJsString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$");
    }

    // 触发延迟保存（用户停止输入 2 秒后自动保存）
    private void triggerSave(String content) {
        this.pendingSaveContent = content;

        if (saveTimer != null) {
            saveTimer.cancel();
        }

        saveTimer = new Timer();
        saveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                performSave();
            }
        }, 2000);
    }

    // 执行保存到数据库
    private void performSave() {
        System.out.println("[DEBUG] performSave: pendingContent=" + (pendingSaveContent != null ? "[" + pendingSaveContent + "](" + pendingSaveContent.length() + "chars)" : "null")
            + ", notebookId=" + currentNotebookId
            + ", title='" + currentTitle + "'");
        if (pendingSaveContent == null || currentNotebookId <= 0 || currentTitle.isEmpty()) {
            System.out.println("[DEBUG] performSave SKIPPED (condition not met)");
            return;
        }
        try {
            DB_Opearte.update_content(currentNotebookId, currentTitle, pendingSaveContent);
            System.out.println("[DEBUG] performSave SUCCESS for '" + currentTitle + "', saved content=[" + pendingSaveContent + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
        pendingSaveContent = null;
    }

    // 立即保存（切换笔记或关闭时调用）
    public void saveNow() {
        System.out.println("[DEBUG] saveNow called, currentTitle=" + currentTitle
            + ", currentNotebookId=" + currentNotebookId);
        if (saveTimer != null) {
            saveTimer.cancel();
            saveTimer = null;
        }
        // 关键修复：主动从 WebView 获取最新内容，不依赖可能过时的 currentMarkdown
        if (initialized && webEngine != null) {
            try {
                Object result = webEngine.executeScript("vditor.getValue()");
                if (result instanceof String) {
                    String latestContent = (String) result;
                    System.out.println("[DEBUG] saveNow: 从 vditor.getValue() 获取到内容 [" + latestContent + "] (" + latestContent.length() + " chars)");
                    pendingSaveContent = latestContent;
                }
            } catch (Exception e) {
                System.out.println("[DEBUG] saveNow: vditor.getValue() 失败，回退到 currentMarkdown");
                if (currentMarkdown != null && !currentMarkdown.isEmpty()) {
                    pendingSaveContent = currentMarkdown;
                }
            }
        } else if (currentMarkdown != null && !currentMarkdown.isEmpty()) {
            pendingSaveContent = currentMarkdown;
        }
        performSave();
    }

    // 关闭编辑器，释放资源（窗口关闭时调用）
    public void shutdown() {
        saveNow();
        // 清理 WebView 防止线程泄漏
        webEngine.load("about:blank");
    }

    // 这个内部类就是 JS 和 Java 通信的桥梁
    public class JavaBridge {
        public void updateContent(String text) {
            currentMarkdown = text;
            System.out.println("[DEBUG] updateContent: received " + (text != null ? text.length() : 0) + " chars, content=[" + text + "]");
            triggerSave(text); // 每次输入变化都触发延迟保存
        }

        public void onReady() {
            System.out.println("Typora 编辑器加载完毕！");
        }
    }
}