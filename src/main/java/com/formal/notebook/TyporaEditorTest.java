package com.formal.notebook;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * TyporaEditorNode 的测试程序
 * 直接运行即可在窗口中测试 Markdown 编辑器
 */
public class TyporaEditorTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 左侧控制面板
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(12));
        leftPanel.setPrefWidth(300);
        leftPanel.setStyle("-fx-background-color: #f8f9fa;");

        Label infoLabel = new Label("Typora 编辑器测试");
        infoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label hintLabel = new Label("编辑右侧 Markdown 文本，\n切换下方单选按钮可测试 setMarkdown()");

        // 预设内容的按钮
        Button loadSampleBtn = new Button("加载示例内容");
        loadSampleBtn.setMaxWidth(Double.MAX_VALUE);

        Button clearBtn = new Button("清空内容");
        clearBtn.setMaxWidth(Double.MAX_VALUE);

        // 状态显示区域
        Label statusLabel = new Label("编辑器状态：");
        TextArea statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setPrefHeight(100);
        statusArea.setText("等待操作...");

        // 手动输入 Markdown 并设置的区域
        Label manualLabel = new Label("手动输入 Markdown：");
        TextArea manualInput = new TextArea();
        manualInput.setPrefHeight(120);
        manualInput.setPromptText("在此输入 Markdown，点击下方按钮设置到编辑器...");

        Button setManualBtn = new Button("设置到编辑器");
        setManualBtn.setMaxWidth(Double.MAX_VALUE);

        leftPanel.getChildren().addAll(
            infoLabel, hintLabel,
            loadSampleBtn, clearBtn,
            statusLabel, statusArea,
            manualLabel, manualInput, setManualBtn
        );

        // 右侧：Typora 编辑器
        TyporaEditorNode editor = new TyporaEditorNode("# 欢迎使用 Typora 风格编辑器\n\n请输入内容...");
        BorderPane editorWrapper = new BorderPane(editor);
        editorWrapper.setPadding(new Insets(0));

        // 底部按钮栏
        HBox bottomBar = new HBox(10);
        bottomBar.setPadding(new Insets(8));
        bottomBar.setStyle("-fx-background-color: #e9ecef;");

        Button getContentBtn = new Button("获取当前内容");
        Button saveBtn = new Button("手动保存");
        Button noteInfoBtn = new Button("设置笔记信息 (id=1, title=测试)");

        bottomBar.getChildren().addAll(getContentBtn, saveBtn, noteInfoBtn);

        // 主布局
        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(editorWrapper);
        root.setBottom(bottomBar);

        // 示例内容
        String sampleMarkdown = "# 示例文档\n\n## 二级标题\n\n这是一段 **粗体** 和 *斜体* 文本。\n\n" +
            "- 列表项 1\n- 列表项 2\n- 列表项 3\n\n" +
            "> 这是一段引用\n\n```java\nSystem.out.println(\"Hello World\");\n```\n\n" +
            "| 列1 | 列2 |\n|-----|-----|\n| A | B |";

        // 事件绑定
        loadSampleBtn.setOnAction(e -> {
            statusArea.setText("加载示例内容...");
            editor.setMarkdown(sampleMarkdown);
            statusArea.setText("已加载示例内容 ✅");
        });

        clearBtn.setOnAction(e -> {
            editor.setMarkdown("");
            statusArea.setText("已清空内容 ✅");
        });

        setManualBtn.setOnAction(e -> {
            String text = manualInput.getText();
            if (!text.isEmpty()) {
                editor.setMarkdown(text);
                statusArea.setText("已设置手动输入的内容 ✅");
            } else {
                statusArea.setText("请输入内容后再点击设置 ⚠️");
            }
        });

        getContentBtn.setOnAction(e -> {
            String content = editor.getMarkdown();
            statusArea.setText("当前编辑器内容：\n" + content.substring(0, Math.min(200, content.length())) + "...");
        });

        saveBtn.setOnAction(e -> {
            // 模拟保存：先设置笔记信息，再保存
            editor.setNoteInfo(1, "测试笔记");
            editor.saveNow();
            statusArea.setText("已执行 saveNow() ✅（数据未实际写入数据库，仅测试 API）");
        });

        noteInfoBtn.setOnAction(e -> {
            editor.setNoteInfo(1, "测试笔记");
            statusArea.setText("已设置笔记信息：notebook_id=1, title=测试笔记 ✅");
        });

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("TyporaEditorNode 测试");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}