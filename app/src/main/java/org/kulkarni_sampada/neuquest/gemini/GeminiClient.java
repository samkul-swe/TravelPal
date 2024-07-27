package org.kulkarni_sampada.neuquest.gemini;

import android.annotation.SuppressLint;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;

public class GeminiClient {
    @SuppressLint("SecretInSource")
    private final GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "AIzaSyCcpzO0cFJfta-KLjTJ2tIaPfeEmxqh4B4");
    private final GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    public GenerativeModelFutures getModel() {
        return model;
    }
}
