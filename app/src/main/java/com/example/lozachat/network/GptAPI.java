package com.example.lozachat.network;
import com.example.lozachat.response.GptRequest;
import com.example.lozachat.response.GptResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
public interface GptAPI {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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"
    })
    @POST("completions")
    Call<GptResponse> createCompletion(@Body GptRequest request);
}
