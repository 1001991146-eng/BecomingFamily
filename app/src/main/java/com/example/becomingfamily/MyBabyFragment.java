package com.example.becomingfamily;
import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MyBabyFragment extends Fragment implements GeminiResponseListener {
    private int week;
    private int days;
    private Activity activity;
    private TextView tvBabySize;
    private ImageView ivBabyImage;
    private TextView tvDevelopment;
    private TextView tvWeeklyTip;
    private TextView tv_Baby_Weeks;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    private boolean isDataLoaded = false;
    private String babySizeContent, developmentContent, weeklyTipContent;

    public MyBabyFragment() {}

    public MyBabyFragment(Activity activity, int week, int days) {
        this.week = week;
        this.activity = activity;
        this.days = days;
    }

    private void startGeminiLoading(String prompt) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (scrollView != null) scrollView.setVisibility(View.GONE);
        new GeminiPrompt(prompt, this);
    }

    @Override
    public void onGeminiSuccess(String rawResponse) {
        isDataLoaded = true;
        if (!isAdded()) return;
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
        try {
            parseAndSaveGeminiResponse(rawResponse);
            updateUiWithLoadedData();
        } catch (Exception e) {
            tv_Baby_Weeks.setText("שגיאה בעיבוד התוכן.");
        }
    }

    @Override
    public void onGeminiFailure(String errorMessage) {
        if (!isAdded()) return;
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
        tv_Baby_Weeks.setText(errorMessage.contains("Quota exceeded") ? 
            "הגעת למגבלת השימוש היומית. ניתן להמשיך מחר." : "שגיאת רשת/API.");
    }

    private void parseAndSaveGeminiResponse(String rawText) {
        final String[] HEADERS = {"SECTION_SIZE_START", "SECTION_DEV_START", "SECTION_TIPS_START"};
        Map<String, String> sections = new HashMap<>();

        for (int i = 0; i < HEADERS.length; i++) {
            String currentHeader = HEADERS[i];
            String nextHeader = (i + 1 < HEADERS.length) ? HEADERS[i + 1] : null;
            int start = rawText.indexOf(currentHeader);
            if (start != -1) {
                int end = (nextHeader != null) ? rawText.indexOf(nextHeader, start + currentHeader.length()) : -1;
                String sectionText = (end != -1) ? rawText.substring(start, end) : rawText.substring(start);
                String cleanText = sectionText.replace(currentHeader, "").trim();
                
                // הסרת נקודתיים אם הופיעו מיד אחרי המזהה
                if (cleanText.startsWith(":") || cleanText.startsWith(" :")) {
                    cleanText = cleanText.substring(cleanText.indexOf(":") + 1).trim();
                }
                
                sections.put(currentHeader, cleanRawText(cleanText));
            }
        }
        babySizeContent = sections.getOrDefault(HEADERS[0], "לא נמצא מידע.");
        developmentContent = sections.getOrDefault(HEADERS[1], "לא נמצא מידע.");
        weeklyTipContent = sections.getOrDefault(HEADERS[2], "לא נמצא מידע.");
    }

    private void updateUiWithLoadedData() {
        tvBabySize.setText(android.text.Html.fromHtml(babySizeContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvDevelopment.setText(android.text.Html.fromHtml(developmentContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvWeeklyTip.setText(android.text.Html.fromHtml(weeklyTipContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tv_Baby_Weeks.setText("המסע המופלא של התינוק");
    }

    private String cleanRawText(String text) {
        if (text == null || text.isEmpty()) return "";
        
        // 1. ניקוי סימני Markdown והדגשות
        String cleaned = text.replaceAll("\\*\\*", "");
        
        // 2. הסרת שורות שמכילות רק נקודה או נקודתיים (שאריות עיצוב של ה-AI)
        cleaned = cleaned.replaceAll("(?m)^\\s*[.:]\\s*$", "");
        
        // 3. הפיכת כוכביות או מקפים לנקודות מעוצבות (•)
        cleaned = cleaned.replaceAll("(?m)^\\s*[*\\-]\\s*", "<br>• ");
        
        // 4. ניקוי שורות רווח מיותרות
        cleaned = cleaned.replaceAll("(<br>\\s*){2,}", "<br>");
        
        cleaned = cleaned.trim();
        if (cleaned.startsWith("<br>")) cleaned = cleaned.replaceFirst("<br>", "");
        
        return cleaned;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_baby, container, false);
        tv_Baby_Weeks = v.findViewById(R.id.tv_Baby_Weeks);
        tvBabySize = v.findViewById(R.id.tvBabySize);
        ivBabyImage = v.findViewById(R.id.ivBabyImage);
        tvDevelopment = v.findViewById(R.id.tvDevelopment);
        tvWeeklyTip = v.findViewById(R.id.tvWeeklyTip);
        progressBar = v.findViewById(R.id.progressBar);
        scrollView = v.findViewById(R.id.scrollView);

        if (isDataLoaded) {
            updateUiWithLoadedData();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
        } else {
            String prompt = getString(R.string.gemini_prompt_baby, week, days);
            tv_Baby_Weeks.setText("טוען נתונים...");
            startGeminiLoading(prompt);
        }
        return v;
    }
}
