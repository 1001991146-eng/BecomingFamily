package com.example.becomingfamily;
import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    // --- STATE MANAGEMENT FIX ---
    private boolean isDataLoaded = false;
    private String babySizeContent, developmentContent, weeklyTipContent;
    // --------------------------

    public MyBabyFragment() {
        // Required empty public constructor
    }

    public MyBabyFragment(Activity activity, int week, int days) {
        this.week = week;
        this.activity = activity;
        this.days = days;
    }

    private void startGeminiLoading(String prompt) {
        Log.d("MARIELA", "Go!!!");
        new GeminiPrompt(prompt, this);
    }

    @Override
    public void onGeminiSuccess(String rawResponse) {
        isDataLoaded = true; // Set the flag
        Log.d("MARIELA", "onGeminiSuccess!!!");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    // Parse the response and save the content
                    parseAndSaveGeminiResponse(rawResponse);
                    // Update the UI with the saved content
                    updateUiWithLoadedData();
                } catch (Exception e) {
                    tv_Baby_Weeks.setText("שגיאה בעיבוד התוכן.");
                }
            });
        }
    }

    @Override
    public void onGeminiFailure(String errorMessage) {
        Log.d("MARIELA", "onGeminiFailure!!!");
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (errorMessage.contains("Quota exceeded")) {
                    tv_Baby_Weeks.setText("הגעת למגבלת השימוש היומית. ניתן להמשיך מחר.");
                } else {
                    tv_Baby_Weeks.setText("שגיאת רשת/API. לא ניתן לטעון מידע .");
                }
            });
        }
    }

    private void parseAndSaveGeminiResponse(String rawText) {
        final String[] HEADERS = {
                "SECTION_SIZE_START",
                "SECTION_DEV_START",
                "SECTION_TIPS_START"
        };
        Map<String, String> sections = new HashMap<>();

        for (int i = 0; i < HEADERS.length; i++) {
            String currentHeader = HEADERS[i];
            String nextHeader = (i + 1 < HEADERS.length) ? HEADERS[i + 1] : null;
            int start = rawText.indexOf(currentHeader);
            if (start != -1) {
                int end = (nextHeader != null) ? rawText.indexOf(nextHeader, start + currentHeader.length()) : -1;
                String sectionText = (end != -1) ? rawText.substring(start, end) : rawText.substring(start);
                String cleanText = sectionText.replace(currentHeader, "").trim();
                sections.put(currentHeader, cleanRawText(cleanText));
            }
        }

        // Save the parsed content to member variables
        babySizeContent = sections.getOrDefault(HEADERS[0], "לא נמצא מידע גודל.");
        developmentContent = sections.getOrDefault(HEADERS[1], "לא נמצא מידע התפתחות.");
        weeklyTipContent = sections.getOrDefault(HEADERS[2], "לא נמצאו טיפים רלוונטיים.");
    }

    private void updateUiWithLoadedData() {
        tvBabySize.setText(android.text.Html.fromHtml(babySizeContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvDevelopment.setText(android.text.Html.fromHtml(developmentContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tvWeeklyTip.setText(android.text.Html.fromHtml(weeklyTipContent, android.text.Html.FROM_HTML_MODE_LEGACY));
        tv_Baby_Weeks.setText("המסע המופלא של התינוק");
    }

    private String cleanRawText(String rawDevelopmentText) {
        if (rawDevelopmentText == null || rawDevelopmentText.isEmpty()) {
            return "אין מידע התפתחות זמין.";
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_baby, container, false);
        tv_Baby_Weeks = v.findViewById(R.id.tv_Baby_Weeks);
        tvBabySize = v.findViewById(R.id.tvBabySize);
        ivBabyImage = v.findViewById(R.id.ivBabyImage);
        tvDevelopment = v.findViewById(R.id.tvDevelopment);
        tvWeeklyTip = v.findViewById(R.id.tvWeeklyTip);

        // --- THE ACTUAL FIX ---
        if (isDataLoaded) {
            // If data is already loaded, just update the UI
            updateUiWithLoadedData();
        } else {
            // Otherwise, start the loading process
            Log.d("MARIELA", "current week: " + Integer.toString(week));
            String prompt = String.format(
                    "אתה יועץ הריון, מומחה להתפתחות עוברית, בעל נימה חיובית ומעודדת. ענה במקצועיות והתמקד אך ורק בתינוק."+                        "ספק סיכום מקיף על התפתחות העובר בשבוע %d יום %d. " +
                            "**חובה לחלק את התשובה לשלושה סעיפים מדויקים. כל סעיף חייב להתחיל במזהה ייחודי ללא תוספות.** " +
                            "המזהים הם: SECTION_SIZE_START, SECTION_DEV_START, SECTION_TIPS_START.\n" +
                            "הסעיפים הם:\n" +
                            "1. SECTION_SIZE_START: תאר את גודלו של העובר במונחי משקל ואורך (בערך), והשווה אותו לפרי נפוץ אחד. **חובה לכתוב סעיף זה כפסקה רציפה אחת. אסור שיופיעו בו אף סימני רשימה, תבליטים, קווים או נקודות כלשהם.**\n" +
                            "2. SECTION_DEV_START: פרט את השינויים הגופניים והתפקודיים המרכזיים שחלים בעובר בשבוע זה. **השתמש בכוכביות בולטות (*) לכל שינוי או איבר מרכזי. לדוגמה: * התפתחות הלב.**\n" +
                            "3. SECTION_TIPS_START: ספק עצה מעשית קצרה ורלוונטית לשבוע זה (למשל, עצה לתמיכה רגשית או משהו שאפשר לנסות בבית). \n" +
                            "המידע חייב להתמקד אך ורק בעובר ובטיפים כלליים/רלוונטיים לשבוע זה.",
                    week,
                    days
            );
            tv_Baby_Weeks.setText("טוען נתונים...");
            startGeminiLoading(prompt);
        }

        return v;
    }
}
