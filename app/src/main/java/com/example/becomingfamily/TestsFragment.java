package com.example.becomingfamily;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    // --- STATE MANAGEMENT FIX ---
    private boolean isDataLoaded = false;
    private String testsContent, resultsContent, upcomingContent;
    // --------------------------

    private static final String[] HEADERS = {
            "SECTION_TESTS_START",
            "SECTION_RESULTS_START",
            "SECTION_UPCOMING_START"
    };
    public TestsFragment() {
        // Required empty public constructor
    }
    public TestsFragment(Activity activity,int week,int days) {
        this.week=week;
        this.activity=activity;
        this.days=days;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        if (isDataLoaded) {
            updateUiWithLoadedData();
        } else {
            tvTitle.setText(String.format("בדיקות והנחיות רפואיות"));
            String prompt = String.format(
                    "אתה יועץ רפואי הריוני. ספק מידע רפואי מדויק ומעודכן על הבדיקות הנדרשות בשבוע %d. " +
                            "**חובה לחלק את התשובה לשלושה סעיפים מדויקים. כל סעיף חייב להתחיל במזהה ייחודי ללא תוספות.** " +
                            "המזהים הם: SECTION_TESTS_START, SECTION_RESULTS_START, SECTION_UPCOMING_START.\n" +
                            "בכל סעיף חובה למלא תוכן רלוונטי ומפורט, ואסור בתכלית האיסור לכתוב 'אין מידע זמין'.**\n" +
                            "שלושת הסעיפים הם:\n" +
                            "1. SECTION_TESTS_START: פרט אילו בדיקות רופא/משרד הבריאות ממליץ לבצע בשבוע זה. " +
                            "**חובה להשתמש בנקודות בולטות (מקף: - ) לכל בדיקה או המלצה. אסור להשאיר מקף ריק או טקסט שאינו שלם לאחר המקף.**\n" +
                            "2. SECTION_RESULTS_START: הסבר באופן כללי מה אומרות תוצאות תקינות ומהן נקודות הדגל האדום שצריך לשים לב אליהן בבדיקות הנפוצות של השלב הזה. " +
                            "**חובה לכתוב סעיף זה כרצף שלם של טקסט - פסקה רציפה אחת ללא הפסקות או קיטועים. " +
                            "אסור בתכלית האיסור להשתמש בסימני רשימה, תבליטים, קווים, מקפים (-), או כוכביות (*).**\n" +
                            "3. SECTION_UPCOMING_START: ספק הצצה קדימה לשבועות הבאים, ציין בקצרה אילו בדיקות מרכזיות מצפות לאם (למשל, סקירות, בדיקות סוכר). " +
                            "**חובה לכתוב סעיף זה כרצף שלם של טקסט - פסקה רציפה אחת ללא הפסקות או קיטועים. " +
                            "אסור בתכלית האיסור להשתמש בסימני רשימה, תבליטים, קווים, מקפים (-), או כוכביות (*).**",
                    week
            );
            tvTestsContent.setText("טוען מידע רפואי... ");
            startGeminiLoading(prompt);
        }
        return v;
    }
    private void startGeminiLoading(String prompt) {
        Log.d("TESTS_FRAG","Sending Prompt: " + prompt);
        new GeminiPrompt(prompt, this);
    }

    @Override
    public void onGeminiSuccess(String rawResponse) {
        isDataLoaded = true;
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    parseAndSaveSections(rawResponse);
                    updateUiWithLoadedData();
                    tvTitle.setText(String.format("בדיקות רפואיות")); // עדכון כותרת סופית
                } catch (Exception e) {
                    Log.e("TESTS_FRAG", "Error parsing content", e);
                    tvTitle.setText("שגיאה בעיבוד התוכן הרפואי.");
                }
            });
        }
    }

    @Override
    public void onGeminiFailure(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.e("TESTS_FRAG", "API Error: " + errorMessage);
                if(errorMessage.contains("Quota exceeded"))
                {
                    tvTitle.setText("הגעת למגבלת השימוש היומית. ניתן להמשיך מחר.");
                }
                else {
                    tvTitle.setText("שגיאת רשת/API. לא ניתן לטעון מידע רפואי.");
                }
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
                sections.put(currentHeader, cleanRawText(cleanText));
            }
        }

        testsContent = sections.getOrDefault(HEADERS[0], "אין מידע זמין על בדיקות שבועיות.");
        resultsContent = sections.getOrDefault(HEADERS[1], "אין מידע זמין על פענוח תוצאות.");
        upcomingContent = sections.getOrDefault(HEADERS[2], "אין מידע זמין על בדיקות עתידיות.");
    }

    private void updateUiWithLoadedData() {
        tvTestsContent.setText(android.text.Html.fromHtml(testsContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvResultsContent.setText(android.text.Html.fromHtml(resultsContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvUpcomingTestsContent.setText(android.text.Html.fromHtml(upcomingContent, android.text.Html.FROM_HTML_MODE_LEGACY));
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
