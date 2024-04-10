package com.example.lozachat.utilities;

import com.example.lozachat.network.GptAPI;
import com.example.lozachat.response.GptChoice;
import com.example.lozachat.response.GptRequest;
import com.example.lozachat.response.GptResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GptChatbot {
    public void sendMessage(String input, final OnMessageReceivedListener listener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GptAPI gptAPI = retrofit.create(GptAPI.class);
        GptRequest request = new GptRequest("gpt-3.5-turbo-instruct",input, 0.2, 50); // You can adjust the temperature and maxTokens as needed
        Call<GptResponse> call = gptAPI.createCompletion(request);
        call.enqueue(new Callback<GptResponse>() {
            @Override
            public void onResponse(Call<GptResponse> call, Response<GptResponse> response) {
                if (response.isSuccessful()) {
                    List<GptChoice> choices = response.body().getChoices();
                    if (choices != null && choices.size() > 0) {
                        String responseText = choices.get(0).getText();
                        listener.onMessageReceived(responseText);
                    } else {
                        listener.onMessageReceived("No response received.");
                    }
                } else {
                    listener.onMessageReceived("Error: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<GptResponse> call, Throwable t) {
                listener.onMessageReceived("Request failed: " + t.getMessage());
            }
        });
    }
    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }
}
