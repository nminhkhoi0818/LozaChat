package com.example.lozachat.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GptResponse {
    @SerializedName("choices")
    private List<GptChoice> choices;
    public List<GptChoice> getChoices() {
        return choices;
    }
}
