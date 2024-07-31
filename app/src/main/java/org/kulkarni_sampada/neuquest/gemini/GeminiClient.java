package org.kulkarni_sampada.neuquest.gemini;

import android.annotation.SuppressLint;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

public class GeminiClient {
    @SuppressLint("SecretInSource")
    private final GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyCcpzO0cFJfta-KLjTJ2tIaPfeEmxqh4B4");
    private final GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    public GenerativeModelFutures getModel() {
        return model;
    }

    public ListenableFuture<GenerateContentResponse> generateResult(String query) {
        GeminiClient geminiClient = new GeminiClient();

        Content content = new Content.Builder()
                .addText(query)
                .build();

        // Get the ListenableFuture from the model
        return geminiClient.getModel().generateContent(content);
    }
}
