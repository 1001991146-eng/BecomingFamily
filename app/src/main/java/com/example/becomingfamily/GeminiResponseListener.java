package com.example.becomingfamily;

public interface GeminiResponseListener {
    void onGeminiSuccess(String rawResponse);
    void onGeminiFailure(String errorMessage);
}
