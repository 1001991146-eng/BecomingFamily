package com.example.becomingfamily;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
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


public class YouFragment extends Fragment implements GeminiResponseListener {
    private Activity activity;
    private int week;
    private int days;
    private String role; // 'Mom' או 'Dad'
    private TextView tvTitle;
    private TextView tvSection1Header;
    private TextView tvSection1Content;
    private TextView tvSection2Header;
    private TextView tvSection2Content;
    private TextView tvSection3Content;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    // --- STATE MANAGEMENT FIX ---
    private boolean isDataLoaded = false;
    private String section1Content, section2Content, section3Content;
    // --------------------------

    private static final String[] HEADERS_MOM = {
            "SECTION_MOM_CHANGES_START",
            "SECTION_MOM_COPING_START",
            "SECTION_COUPLE_TIP_START"
    };

    private static final String[] HEADERS_DAD = {
            "SECTION_DAD_ROLE_START",
            "SECTION_DAD_SUPPORT_START",
            "SECTION_COUPLE_TIP_START"
    };
    public YouFragment()
    {

    }
    public YouFragment(Activity activity, int week, int days, String role) {
        this.week=week;
        this.role=role;
        this.activity=activity;
        this.days=days;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDataLoaded", isDataLoaded);
        outState.putString("section1Content", section1Content);
        outState.putString("section2Content", section2Content);
        outState.putString("section3Content", section3Content);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_you, container, false);

        tvTitle = v.findViewById(R.id.tv_Partner_And_Me_Title);
        tvSection1Header = v.findViewById(R.id.tvSection1Header);
        tvSection1Content = v.findViewById(R.id.tvSection1Content);
        tvSection2Header = v.findViewById(R.id.tvSection2Header);
        tvSection2Content = v.findViewById(R.id.tvSection2Content);
        tvSection3Content = v.findViewById(R.id.tvSection3Content);
        progressBar = v.findViewById(R.id.progressBar);
        scrollView = v.findViewById(R.id.scrollView);

        updateUIForRole(role);

        if (savedInstanceState != null) {
            isDataLoaded = savedInstanceState.getBoolean("isDataLoaded");
            section1Content = savedInstanceState.getString("section1Content");
            section2Content = savedInstanceState.getString("section2Content");
            section3Content = savedInstanceState.getString("section3Content");
        }

        if (isDataLoaded) {
            updateUiWithLoadedData();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
        } else {
            String prompt = "";
            if (role.equals("Mom"))
            {
                prompt = getString(R.string.gemini_prompt_mom, week, days);
            }
            else { // Dad
                prompt = getString(R.string.gemini_prompt_dad, week);
            }
            tvTitle.setText(String.format(" טוען נתונים עבור %s..." , role.equals("Mom") ? "האם" : "האב"));
            startGeminiLoading(prompt);
        }

        return v;
    }
    private void updateUIForRole(String role) {
        if (role.equals("Mom")) {
            tvTitle.setText(String.format("שבוע %d: הריון, הגוף והרגש", week));
            tvSection1Header.setText("השינויים אצלך, אמא");
            tvSection2Header.setText("איך להתמודד ולהתכונן");
        } else { // Dad
            tvTitle.setText(String.format("שבוע %d: תמיכה והכנה לבן/בת הזוג", week));
            tvSection1Header.setText("תפקידך המרכזי בשבוע זה");
            tvSection2Header.setText("כיצד לתמוך באמא");
        }
    }


    private void startGeminiLoading(String prompt) {
        Log.d("PARTNER_FRAG","Sending Prompt: " + prompt);
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
            parseAndSaveSections(rawResponse, role);
            updateUiWithLoadedData();
            if(role.equals("Mom"))
            {
                tvTitle.setText("לדאוג לעצמך: צעד אחר צעד");
            }
            else{
                tvTitle.setText("משפחה גדלה: המקום שלך במסע"); // עדכון כותרת סופית
            }
        } catch (Exception e) {
            Log.e("PARTNER_FRAG", "Error parsing content", e);
            tvTitle.setText("שגיאה בעיבוד התוכן.");
        }
    }

    @Override
    public void onGeminiFailure(String errorMessage) {
        if (!isAdded()) return;
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
        Log.e("PARTNER_FRAG", "API Error: " + errorMessage);
        if(errorMessage.contains("Quota exceeded"))
        {
            tvTitle.setText("הגעת למגבלת השימוש היומית. ניתן להמשיך מחר.");
        }
        else {
            tvTitle.setText("שגיאת רשת/API. נסה שוב.");
        }
    }

    private void parseAndSaveSections(String rawText, String role) {
        String[] currentHeaders = role.equals("Mom") ? HEADERS_MOM : HEADERS_DAD;
        Map<String, String> sections = new HashMap<>();

        for (int i = 0; i < currentHeaders.length; i++) {
            String currentHeader = currentHeaders[i];
            String nextHeader = (i + 1 < currentHeaders.length) ? currentHeaders[i+1] : null;
            int start = rawText.indexOf(currentHeader);
            if (start != -1) {
                int end = (nextHeader != null) ? rawText.indexOf(nextHeader, start + currentHeader.length()) : -1;
                String sectionText = (end != -1) ? rawText.substring(start, end) : rawText.substring(start);
                String cleanText = sectionText.replace(currentHeader, "").trim();
                sections.put(currentHeader, cleanRawText(cleanText));
            }
        }

        section1Content = sections.getOrDefault(currentHeaders[0], "לא נמצא מידע.");
        section2Content = sections.getOrDefault(currentHeaders[1], "לא נמצא מידע.");
        section3Content = sections.getOrDefault(currentHeaders[2], "לא נמצא טיפ זוגי.");
    }

    private void updateUiWithLoadedData() {
        tvSection1Content.setText(android.text.Html.fromHtml(section1Content, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvSection2Content.setText(android.text.Html.fromHtml(section2Content, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvSection3Content.setText(android.text.Html.fromHtml(section3Content, android.text.Html.FROM_HTML_MODE_LEGACY));
    }

    private String cleanRawText(String rawDevelopmentText) {
        if (rawDevelopmentText == null || rawDevelopmentText.isEmpty()) {
            return "אין מידע זמין.";
        }

        String cleaned = rawDevelopmentText.trim();

        cleaned = cleaned.replaceAll("[*\\-][\\s*]", "<br>• ");
        cleaned = cleaned.replaceAll("(\\*\\*[^\\*]+\\*\\*)", "<br><br><b>$1</b>");
        cleaned = cleaned.replaceAll("\\*\\*", "");

        if (cleaned.startsWith("<br>")) {
            cleaned = cleaned.replaceFirst("<br>", "");
        }

        return cleaned;
    }
}
