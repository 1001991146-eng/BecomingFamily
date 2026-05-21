package com.example.becomingfamily;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;


public class TestsFragment extends Fragment implements GeminiResponseListener {

    private Activity activity;
    private int week;
    private int days;
    private TextView tvTitle;
    private TextView tvTestsHeader;
    private TextView tvTestsContent;
    private TextView tvResultsHeader;
    private TextView tvResultsContent;
    private TextView tvUpcomingTestsContent;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    private boolean isDataLoaded = false;
    private String testsContent, resultsContent, upcomingContent;

    private static final String[] HEADERS = {
            "SECTION_TESTS_START",
            "SECTION_RESULTS_START",
            "SECTION_UPCOMING_START"
    };
    public TestsFragment() {}
    public TestsFragment(Activity activity,int week,int days) {
        this.week=week;
        this.activity=activity;
        this.days=days;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_tests, container, false);
        tvTitle = v.findViewById(R.id.tv_Tests_Title);
        tvTestsHeader = v.findViewById(R.id.tvTestsHeader);
        tvTestsContent = v.findViewById(R.id.tvTestsContent);
        tvResultsHeader = v.findViewById(R.id.tvResultsHeader);
        tvResultsContent = v.findViewById(R.id.tvResultsContent);
        tvUpcomingTestsContent = v.findViewById(R.id.tvUpcomingTestsContent);
        progressBar = v.findViewById(R.id.progressBar);
        scrollView = v.findViewById(R.id.scrollView);

        if (isDataLoaded) {
            updateUiWithLoadedData();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText("בדיקות והנחיות רפואיות");
            String prompt = getString(R.string.gemini_prompt_tests, week);
            tvTestsContent.setText("טוען מידע רפואי... ");
            startGeminiLoading(prompt);
        }
        return v;
    }
    private void startGeminiLoading(String prompt) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (scrollView != null) scrollView.setVisibility(View.GONE);
        new GeminiPrompt(prompt, this);
    }

    @Override
    public void onGeminiSuccess(String rawResponse) {
        isDataLoaded = true;
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
                try {
                    parseAndSaveSections(rawResponse);
                    updateUiWithLoadedData();
                    tvTitle.setText("בדיקות רפואיות");
                } catch (Exception e) {
                    tvTitle.setText("שגיאה בעיבוד התוכן הרפואי.");
                }
            });
        }
    }

    @Override
    public void onGeminiFailure(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
                tvTitle.setText(errorMessage.contains("Quota exceeded") ? 
                    "הגעת למגבלת השימוש היומית. ניתן להמשיך מחר." : "שגיאת רשת/API.");
            });
        }
    }

    private void parseAndSaveSections(String rawText) {
        Map<String, String> sections = new HashMap<>();

        for (int i = 0; i < HEADERS.length; i++) {
            String currentHeader = HEADERS[i];
            String nextHeader = (i + 1 < HEADERS.length) ? HEADERS[i+1] : null;
            int start = rawText.indexOf(currentHeader);
            if (start != -1) {
                int end = (nextHeader != null) ? rawText.indexOf(nextHeader, start + currentHeader.length()) : -1;
                String sectionText = (end != -1) ? rawText.substring(start, end) : rawText.substring(start);
                String cleanText = sectionText.replace(currentHeader, "").trim();
                
                if (cleanText.startsWith(":") || cleanText.startsWith(" :")) {
                    cleanText = cleanText.substring(cleanText.indexOf(":") + 1).trim();
                }
                
                sections.put(currentHeader, cleanRawText(cleanText));
            }
        }

        testsContent = sections.getOrDefault(HEADERS[0], "אין מידע זמין.");
        resultsContent = sections.getOrDefault(HEADERS[1], "אין מידע זמין.");
        upcomingContent = sections.getOrDefault(HEADERS[2], "אין מידע זמין.");
    }

    private void updateUiWithLoadedData() {
        tvTestsContent.setText(android.text.Html.fromHtml(testsContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvResultsContent.setText(android.text.Html.fromHtml(resultsContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvUpcomingTestsContent.setText(android.text.Html.fromHtml(upcomingContent, android.text.Html.FROM_HTML_MODE_LEGACY));
    }

    private String cleanRawText(String text) {
        if (text == null || text.isEmpty()) return "";
        String cleaned = text.replaceAll("\\*\\*", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*[.:]\\s*$", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*[*\\-]\\s*", "<br>• ");
        cleaned = cleaned.replaceAll("(<br>\\s*){2,}", "<br>");
        cleaned = cleaned.trim();
        if (cleaned.startsWith("<br>")) cleaned = cleaned.replaceFirst("<br>", "");
        return cleaned;
    }
}
