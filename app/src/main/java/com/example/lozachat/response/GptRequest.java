package com.example.lozachat.response;

import com.google.gson.annotations.SerializedName;

public class GptRequest {
    @SerializedName("model")
    private String model;
    @SerializedName("prompt")
    private String prompt;
    @SerializedName("temperature")
    private double temperature;
    @SerializedName("max_tokens")
    private int maxTokens;
    public GptRequest(String model, String prompt, double temperature, int maxTokens) {
        this.model = model;
        this.prompt = prompt;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }
}
