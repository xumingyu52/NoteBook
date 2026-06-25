package com.formal.notebook;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;

public class ApiConfigDialog {
    
    private Stage dialogStage;
    private TextField nameField;
    private TextField baseUrlField;
    private TextField modelIdField;
    private PasswordField apiKeyField;
    private ListView<ApiConfig> configListView;
    private Button addButton;
    private Button deleteButton;
    private Button saveButton;
    private Button cancelButton;
    
    public void show() {
        dialogStage = new Stage();
        dialogStage.setTitle("API 配置管理");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setWidth(700);
        dialogStage.setHeight(500);
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        
        configListView = new ListView<>();
        configListView.setPrefWidth(200);
        loadConfigs();
        
        VBox formBox = new VBox(12);
        formBox.setPadding(new Insets(0, 0, 0, 16));
        
        Label titleLabel = new Label("添加/编辑模型");
        titleLabel.setFont(Font.font(16));
        formBox.getChildren().add(titleLabel);
        
        VBox configSection = new VBox(8);
        configSection.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 12; -fx-border-radius: 4;");
        
        Label sectionLabel = new Label("自定义配置");
        sectionLabel.setFont(Font.font(14));
        configSection.getChildren().add(sectionLabel);
        
        nameField = new TextField();
        nameField.setPromptText("模型名称（如：GPT-4）");
        configSection.getChildren().add(createLabeledField("模型名称", nameField));
        
        baseUrlField = new TextField();
        baseUrlField.setPromptText("e.g. https://api.openai.com/v1");
        configSection.getChildren().add(createLabeledField("自定义请求地址", baseUrlField));
        
        Label tipLabel = new Label("请填写兼容 OpenAI API 的服务端点地址，不要以斜杠结尾。/chat/completions 将会被补充到你填写的地址末尾。");
        tipLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        tipLabel.setWrapText(true);
        configSection.getChildren().add(tipLabel);
        
        modelIdField = new TextField();
        modelIdField.setPromptText("输入模型 ID");
        configSection.getChildren().add(createLabeledField("模型 ID", modelIdField));
        
        apiKeyField = new PasswordField();
        apiKeyField.setPromptText("输入 API 密钥");
        configSection.getChildren().add(createLabeledField("API 密钥", apiKeyField));
        
        formBox.getChildren().add(configSection);
        
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        addButton = new Button("新增");
        addButton.setOnAction(e -> addConfig());
        
        deleteButton = new Button("删除");
        deleteButton.setOnAction(e -> deleteConfig());
        
        saveButton = new Button("保存");
        saveButton.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        saveButton.setOnAction(e -> saveConfig());
        
        cancelButton = new Button("取消");
        cancelButton.setOnAction(e -> dialogStage.close());
        
        buttonBox.getChildren().addAll(addButton, deleteButton, saveButton, cancelButton);
        formBox.getChildren().add(buttonBox);
        
        root.setLeft(configListView);
        root.setCenter(formBox);
        
        configListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });
        
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    private VBox createLabeledField(String label, TextField field) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        box.getChildren().addAll(lbl, field);
        return box;
    }
    
    private void loadConfigs() {
        configListView.getItems().clear();
        try {
            ApiConfigDao.createTable();
            ArrayList<ApiConfig> configs = ApiConfigDao.getAllConfigs();
            configListView.getItems().addAll(configs);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("加载配置失败", e.getMessage());
        }
    }
    
    private void fillForm(ApiConfig config) {
        nameField.setText(config.getName());
        baseUrlField.setText(config.getBaseUrl());
        modelIdField.setText(config.getModelId());
        apiKeyField.setText(config.getApiKey());
    }
    
    private void clearForm() {
        nameField.clear();
        baseUrlField.clear();
        modelIdField.clear();
        apiKeyField.clear();
        configListView.getSelectionModel().clearSelection();
    }
    
    private void addConfig() {
        clearForm();
    }
    
    private void deleteConfig() {
        ApiConfig selected = configListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("错误", "请先选择一个配置");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("确定要删除这个配置吗？");
        alert.setContentText("删除后无法恢复");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                ApiConfigDao.deleteConfig(selected.getId());
                loadConfigs();
                clearForm();
            } catch (SQLException e) {
                showError("删除失败", e.getMessage());
            }
        }
    }
    
    private void saveConfig() {
        String name = nameField.getText().trim();
        String baseUrl = baseUrlField.getText().trim();
        String modelId = modelIdField.getText().trim();
        String apiKey = apiKeyField.getText().trim();
        
        if (name.isEmpty()) {
            showError("错误", "请输入模型名称");
            return;
        }
        if (baseUrl.isEmpty()) {
            showError("错误", "请输入自定义请求地址");
            return;
        }
        if (modelId.isEmpty()) {
            showError("错误", "请输入模型 ID");
            return;
        }
        if (apiKey.isEmpty()) {
            showError("错误", "请输入 API 密钥");
            return;
        }
        
        ApiConfig selected = configListView.getSelectionModel().getSelectedItem();
        
        try {
            if (selected != null) {
                selected.setName(name);
                selected.setBaseUrl(baseUrl);
                selected.setModelId(modelId);
                selected.setApiKey(apiKey);
                ApiConfigDao.updateConfig(selected);
            } else {
                ApiConfig config = new ApiConfig(name, baseUrl, modelId, apiKey);
                ApiConfigDao.addConfig(config);
            }
            loadConfigs();
            showSuccess("保存成功");
        } catch (SQLException e) {
            showError("保存失败", e.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setContentText(message);
        alert.showAndWait();
    }
}