package com.example.becomingfamily;

import android.util.Log;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiPrompt {

    private final GeminiResponseListener listener;

    public GeminiPrompt(String prompt, GeminiResponseListener listener) {
        this.listener = listener;

        GenerativeModel gm = new GenerativeModel(
                // Use a standard, stable model name compatible with the new library
                "gemini-2.5-flash",
                BuildConfig.GEMINI_API_KEY
        );
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText(prompt).build();




        // An executor is needed to handle the asynchronous response.
        Executor mainExecutor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // The new SDK provides a direct getText() method on the response.
                String responseText = result.getText();
                if (responseText != null) {
                    listener.onGeminiSuccess(responseText);
                } else {
                    // Handle cases where the response might be empty.
                    onFailure(new Throwable("Response was empty."));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("MARIELA", "Gemini Failure " + t.getMessage());
                listener.onGeminiFailure(t.getMessage());
            }
        }, mainExecutor);
    }
}
