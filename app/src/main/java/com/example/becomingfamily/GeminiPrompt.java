package com.example.becomingfamily;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiPrompt {
    // צור ExecutorService גלובלי או בתוך המתודה, כדי להריץ את ה-Callback
    private final GeminiResponseListener listener; // המאזין
    private final ListenableFuture<GenerateContentResponse> responseFuture;
    // הקונסטרקטור מקבל Listener, לא TextView
    public GeminiPrompt(String prompt, GeminiResponseListener listener)
    {
        this.listener = listener;
        // ***** 1. הגדרת המודל וה-Content (הקוד החסר) *****
        String apiKey=BuildConfig.GEMINI_API_KEY;
        // יצירת המודל הרגיל
        GenerativeModel gm = new GenerativeModel(
                "gemini-2.0-flash",
                apiKey
        );
        // עטיפת המודל באובייקט התומך ב-Futures
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        // יצירת אובייקט ה-Content
        Content content = new Content.Builder().addText(prompt).build();
        // 1. הגדרת המודל וה-Future
        // ... (קוד יצירת GenerativeModelFutures model ו-Content content) ...
        this.responseFuture = model.generateContent(content);
        // 2. הפעלת ה-Callback
        loadPregnancyInfo();
    }

    public void loadPregnancyInfo() {
        Futures.addCallback(this.responseFuture, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String geminiResponseText = result.getText();
                        // מריץ את ה-Callback ב-Listener
                        listener.onGeminiSuccess(geminiResponseText);
                    }
                    @Override
                    public void onFailure(Throwable t) {
                        // מריץ את ה-Callback של הכשל
                        listener.onGeminiFailure(t.getMessage());
                    }
                },
                // חשוב! אנו חייבים להשתמש ב-Executor שרץ ב-UI Thread כדי שה-onSuccess/onFailure
                // ירוצו בסביבה שבה ניתן לעדכן UI. נשתמש ב-Main Executor של האפליקציה.
                // לשם כך, אנו זקוקים ל-Context או לאובייקט שמחזיר את ה-Main Executor.
                // נניח שאתה מקבל Activity/Context לקונסטרקטור כדי לחלץ את ה-Main Executor:
                // ContextCompat.getMainExecutor(context)
                Executors.newSingleThreadExecutor() // נשתמש ב-Executor רגיל, וה-Fragment יחזור ל-UI Thread בעצמו
        );
    }/*

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ListenableFuture<GenerateContentResponse> responseFuture;
    private Activity activity;
    private TextView tvTarget;
    public GeminiPrompt(Activity activity, String prompt , TextView tvTarget)
    {
        this.activity=activity;
        this.tvTarget=tvTarget;
        String apiKey=BuildConfig.GEMINI_API_KEY;
                // this.context = context;
                // 1. יצירת המודל הרגיל
        GenerativeModel gm = new GenerativeModel(
                        "gemini-2.0-flash",
                        apiKey
        );

        // ... קוד אתחול המודל ...
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder().addText(prompt).build();

        // ******* אתחול המשתנה (ללא המילה ListenableFuture) *******
        this.responseFuture = model.generateContent(content);

        // אנו יכולים לקרוא למתודה המטפלת כאן מיד
        loadPregnancyInfo();
    }

    // הוסר הפרמטר String prompt כיוון שה-responseFuture כבר נוצר
    public void loadPregnancyInfo() {

        // עכשיו responseFuture נגיש!
        Futures.addCallback(this.responseFuture, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        Log.d("MARIELA","got from gemini:"+result.getText().toString());
                        String geminiResponseText = result.getText();

                        // *** 🎯 התיקון: עוטפים את עדכון ה-UI ב-runOnUiThread ***
                        activity.runOnUiThread(() -> {
                            tvTarget.setText(geminiResponseText);
                            Log.d("MARIELA", "Gemini success and UI updated.");
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d("MARIELA","failed to get from gemini:"+t.getMessage().toString());
                        // *** עוטפים גם את ה-Failure ב-runOnUiThread ***
                        activity.runOnUiThread(() -> {
                            Log.e("MARIELA", "Gemini error: " + t.getMessage());
                            tvTarget.setText("שגיאת טעינה: " + t.getMessage());
                        });                    }
                    // ... (קוד onSuccess ו-onFailure) ...

                },
                // כדי שהקוד יעבוד, עליך להשתמש ב-Context שהועבר לקונסטרקטור
                // ContextCompat.getMainExecutor(this.context)
                // משתמשים ב-Executor הסטנדרטי שלנו לביצוע ה-Callback עצמו (ב-Thread רקע)
                Executors.newSingleThreadExecutor()        );
    }
    */
}


