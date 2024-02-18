package com.parishod.watomatic.service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;

public class ApiService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client;

    public interface ApiResponseCallback {
        void onSuccess(String responseData);
        void onFailure(Exception e);
    }

    public ApiService() {
        // Initialize OkHttpClient with custom timeout settings
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES) // Set the connection timeout
                .readTimeout(5, TimeUnit.MINUTES)    // Set the read timeout
                .writeTimeout(5, TimeUnit.MINUTES)   // Set the write timeout
                .build();
    }

    public void postQuestion(String question, ApiResponseCallback callback) {
        new Thread(() -> {
            try {
                String url = "http://192.168.31.184:7091/api/answer";
                String json = createJsonBody(question);

                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Accept", "*/*")
                        .addHeader("Connection", "keep-alive")
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                // Assuming the response body is not null and is successful
                String responseData = response.body().string();
                // Pass the response data to the callback
                callback.onSuccess(responseData);
            } catch (Exception e) {
                e.printStackTrace();
                // Pass the error to the callback
                callback.onFailure(e);
            }
        }).start();
    }

    private String createJsonBody(String question) {
        return "{\"question\":\"" + question + "\","
                + "\"api_key\":\"\","
                + "\"embeddings_key\":\"\","
                + "\"active_docs\":\"local/5_Cara_Budidaya_Kepiting_Bakau_di_Rumah_yang_Cocok_bagi_Pemula___kumparan.com.pdf/\","
                + "\"history\":[],"
                + "\"conversation_id\":null,"
                + "\"prompt_id\":\"default\"}";
    }
}
