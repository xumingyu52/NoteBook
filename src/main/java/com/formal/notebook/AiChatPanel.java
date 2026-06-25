package com.formal.notebook;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiChatPanel extends VBox {
    
    private TextArea chatArea;
    private TextField inputField;
    private Button sendButton;
    private Button closeButton;
    private Label statusLabel;
    private AiService aiService;
    private Runnable onContentGenerated;
    private String currentContext;
    private String lastResponse;
    
    public AiChatPanel() {
        initUI();
    }
    
    public void setOnContentGenerated(Runnable onContentGenerated) {
        this.onContentGenerated = onContentGenerated;
    }
    
    private void initUI() {
        this.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1;");
        this.setPrefWidth(350);
        
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 12;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("AI 助手");
        title.setFont(Font.font(14));
        title.setStyle("-fx-font-weight: bold;");
        
        closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 16;");
        closeButton.setOnAction(e -> this.setVisible(false));
        
        HBox.setHgrow(title, Priority.ALWAYS);
        header.getChildren().addAll(title, closeButton);
        
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setStyle("-fx-font-family: 'SimSun', 'STSong', serif; -fx-font-size: 14;");
        chatArea.setWrapText(true);
        
        statusLabel = new Label("就绪");
        statusLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        statusLabel.setAlignment(Pos.CENTER);
        
        HBox inputBox = new HBox(8);
        inputBox.setPadding(new Insets(12));
        
        inputField = new TextField();
        inputField.setPromptText("输入指令，如：帮我总结这段笔记...");
        inputField.setOnAction(e -> sendMessage());
        
        sendButton = new Button("发送");
        sendButton.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        sendButton.setOnAction(e -> sendMessage());
        
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputBox.getChildren().addAll(inputField, sendButton);
        
        this.getChildren().addAll(header, chatArea, statusLabel, inputBox);
        VBox.setVgrow(chatArea, Priority.ALWAYS);
        
        chatArea.setText("点击右下角 AI 按钮开始使用 AI 助手\n\n功能：\n• 帮我总结这段笔记\n• 帮我续写内容\n• 帮我整理格式");
        sendButton.setDisable(true);
    }
    
    public void setContext(String context) {
        this.currentContext = context;
    }
    
    public void setService(AiService service) {
        this.aiService = service;
    }
    
    public void show() {
        sendButton.setDisable(false);
        chatArea.setText("");
        inputField.requestFocus();
        showStatus("就绪");
    }
    
    private void sendMessage() {
        String prompt = inputField.getText().trim();
        if (prompt.isEmpty()) return;
        if (aiService == null) {
            showStatus("错误：未配置 API");
            return;
        }
        
        chatArea.appendText("你: " + prompt + "\n\n");
        inputField.clear();
        sendButton.setDisable(true);
        showStatus("AI 正在思考...");
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                String response = aiService.chat(prompt, currentContext);
                this.lastResponse = response;
                Platform.runLater(() -> {
                    chatArea.appendText("AI: " + response + "\n\n");
                    if (onContentGenerated != null) {
                        onContentGenerated.run();
                    }
                    showStatus("完成");
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    chatArea.appendText("错误: " + e.getMessage() + "\n\n");
                    showStatus("错误");
                    sendButton.setDisable(false);
                });
            } finally {
                executor.shutdown();
            }
        });
    }
    
    private void showStatus(String status) {
        statusLabel.setText(status);
    }
    
    public String getAiResponse() {
        return lastResponse != null ? lastResponse : "";
    }
}