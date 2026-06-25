package com.formal.notebook;

public class ApiConfig {
    private int id;
    private String name;
    private String baseUrl;
    private String modelId;
    private String apiKey;
    
    public ApiConfig() {}
    
    public ApiConfig(String name, String baseUrl, String modelId, String apiKey) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.modelId = modelId;
        this.apiKey = apiKey;
    }
    
    public ApiConfig(int id, String name, String baseUrl, String modelId, String apiKey) {
        this.id = id;
        this.name = name;
        this.baseUrl = baseUrl;
        this.modelId = modelId;
        this.apiKey = apiKey;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}