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
    // הגדרת כותרות קבועות ל-Parsing (חובה להתאים לפרומפט ב-PregnancyPromptGenerator)
    private static final String[] HEADERS = {
            "**הבדיקות הרלוונטיות לשבוע זה**",
            "**כיצד להבין את התוצאות**",
            "**הבדיקות הבאות**"
    };
    public TestsFragment(Activity activity,int week,int days) {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_tests, container, false);
        // אתחול רכיבי ה-UI
        tvTitle = v.findViewById(R.id.tv_Tests_Title);
        tvTestsHeader = v.findViewById(R.id.tvTestsHeader);
        tvTestsContent = v.findViewById(R.id.tvTestsContent);
        tvResultsHeader = v.findViewById(R.id.tvResultsHeader);
        tvResultsContent = v.findViewById(R.id.tvResultsContent);
        tvUpcomingTestsContent = v.findViewById(R.id.tvUpcomingTestsContent);

        // עדכון כותרת ראשונית
        tvTitle.setText(String.format("שבוע %d: בדיקות והנחיות רפואיות", week));


        // יצירת הפרומפט וטעינת הנתונים
        String prompt = String.format(
                "אתה יועץ רפואי הריוני. ספק מידע רפואי מדויק על הבדיקות הנדרשות בשבוע %d יום %d. " +
                        "**חובה לחלק את התשובה לשלושה סעיפים מדויקים באמצעות כותרות בולטות (Markdown headers) בלבד.** " +
                        "שלושת הסעיפים הם:\n" +
                        "1. **הבדיקות הרלוונטיות לשבוע זה**: פרט אילו בדיקות רופא/משרד הבריאות ממליץ לבצע בשבוע זה. **השתמש בנקודות בולטות (-).**\n" +
                        "2. **כיצד להבין את התוצאות**: הסבר באופן כללי מה אומרות תוצאות תקינות ומהן נקודות הדגל האדום שצריך לשים לב אליהן בבדיקות הנפוצות של השלב הזה. \n" +
                        "3. **הבדיקות הבאות**: ספק הצצה קדימה לשבועות הבאים, ציין בקצרה אילו בדיקות מרכזיות מצפות לאם (למשל, סקירות, בדיקות סוכר).",
                week,
                days
        );

        // הצגת סטטוס טעינה
        tvTestsContent.setText("טוען מידע רפואי... ");

        startGeminiLoading(prompt);
        return v;
    }
    private void startGeminiLoading(String prompt) {
        Log.d("TESTS_FRAG","Sending Prompt: " + prompt);
        // יש לוודא ש-GeminiPrompt זמין ומשתמש ב-this כמאזין
        new GeminiPrompt(prompt, this);
    }

    // ********* יישום ממשק GeminiResponseListener *********

    @Override
    public void onGeminiSuccess(String rawResponse) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    parseAndDisplaySections(rawResponse);
                    tvTitle.setText(String.format("שבוע %d: בדיקות רפואיות", week)); // עדכון כותרת סופית
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
                tvTitle.setText("שגיאת רשת/API. לא ניתן לטעון מידע רפואי.");
            });
        }
    }

    // ********* לוגיקת ה-Parsing (מותאמת מהקוד הקיים) *********
    private void parseAndDisplaySections(String rawText) {
        Map<String, String> sections = new HashMap<>();

        // 1. לולאת ה-Parsing לחילוץ המקטעים
        for (int i = 0; i < HEADERS.length; i++) {
            String currentHeader = HEADERS[i];
            String nextHeader = (i + 1 < HEADERS.length) ? HEADERS[i+1] : null;

            int start = rawText.indexOf(currentHeader);
            int end = -1;

            if (start != -1) {
                if (nextHeader != null) {
                    end = rawText.indexOf(nextHeader, start + currentHeader.length());
                }

                String sectionText;
                if (end != -1) {
                    sectionText = rawText.substring(start, end);
                } else {
                    sectionText = rawText.substring(start);
                }

                String cleanText = sectionText.replace(currentHeader, "").trim();
                String finalCleanText = cleanRawText(cleanText);
                sections.put(currentHeader, finalCleanText);
            }
        }

        // 2. עדכון ה-UI

        // מקטע 1: בדיקות רלוונטיות
        String content1 = sections.getOrDefault(HEADERS[0], "אין מידע זמין על בדיקות שבועיות.");
        tvTestsContent.setText(android.text.Html.fromHtml(content1, android.text.Html.FROM_HTML_MODE_LEGACY));

        // מקטע 2: פענוח תוצאות
        String content2 = sections.getOrDefault(HEADERS[1], "אין מידע זמין על פענוח תוצאות.");
        tvResultsContent.setText(android.text.Html.fromHtml(content2, android.text.Html.FROM_HTML_MODE_LEGACY));

        // מקטע 3: הבדיקות הבאות
        String content3 = sections.getOrDefault(HEADERS[2], "אין מידע זמין על בדיקות עתידיות.");
        tvUpcomingTestsContent.setText(android.text.Html.fromHtml(content3, android.text.Html.FROM_HTML_MODE_LEGACY));
    }

    // ********* פונקציית הניקוי המשותפת (שתיקנת והוכחה כיעילה) *********
    private String cleanRawText(String rawDevelopmentText) {
        if (rawDevelopmentText == null || rawDevelopmentText.isEmpty()) {
            return "אין מידע זמין.";
        }

        // 1. הסר את כל המופעים של '###' ואת כל הטקסט שאחריהם (ניקוי סוף התגובה)
        String cleaned = rawDevelopmentText.replaceAll("###.*", "").trim();

        // 2. החלף בולטים (כוכביות או מקפים) בשבירת שורה כפולה (<br>• )
        // הערה: נשתמש ב-trim() לאחר החלפה כדי לנקות רווח מיותר לפני • אם קיים
        cleaned = cleaned.replaceAll("[*\\-][\\s*]", "<br>• ").trim();

        // 3. החלף כותרות משנה מודגשות ע"י הוספת שבירת שורה לפניהן והדגשה
        cleaned = cleaned.replaceAll("(\\*\\*[^\\*]+\\*\\*)", "<br><br><b>$1</b>");

        // 4. סיום ניקוי: הסר את תגי ה-** סביב הכותרות שנותרו
        cleaned = cleaned.replaceAll("\\*\\*", "");

        // 5. הסר את שבירת השורה אם נוצרה בתחילת הטקסט
        if (cleaned.startsWith("<br>")) {
            // החלף רק את המופע הראשון של <br>
            cleaned = cleaned.replaceFirst("<br>", "").trim();
        }

        return cleaned;
    }
}