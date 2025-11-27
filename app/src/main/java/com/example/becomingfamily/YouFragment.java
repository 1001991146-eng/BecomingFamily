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

    // הגדרת כותרות קבועות ל-Parsing (חובה להתאים לכותרות שנשלחות לפרומפט)
    private static final String[] HEADERS_MOM = {
            "**השינויים אצלך, אמא**",
            "**איך להתמודד ולהתכונן**",
            "**טיפ זוגי לשבוע זה**"
    };

    private static final String[] HEADERS_DAD = {
            "**תפקידך המרכזי בשבוע זה**",
            "**כיצד לתמוך באמא**",
            "**טיפ זוגי לשבוע זה**"
    };
    public YouFragment()
    {

    }
    public YouFragment(Activity activity, int week, int days, String role) {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_you, container, false);

        // אתחול רכיבי ה-UI
        tvTitle = v.findViewById(R.id.tv_Partner_And_Me_Title);
        tvSection1Header = v.findViewById(R.id.tvSection1Header);
        tvSection1Content = v.findViewById(R.id.tvSection1Content);
        tvSection2Header = v.findViewById(R.id.tvSection2Header);
        tvSection2Content = v.findViewById(R.id.tvSection2Content);
        tvSection3Content = v.findViewById(R.id.tvSection3Content);

        // עדכון הכותרות והטקסט הראשוני בהתאם לתפקיד
        updateUIForRole(role);
        // יצירת הפרומפט וטעינת הנתונים

        String prompt="";
        if (role.equals("Mom"))
        {
             prompt = String.format(
                    "ספק מידע על השינויים הפיזיים והרגשיים של האישה בשבוע %d יום %d. " +
                            "**חובה לחלק את התשובה לשלושה סעיפים מדויקים באמצעות כותרות בולטות (Markdown headers) בלבד.** " +
                            "שלושת הסעיפים הם:\n" +
                            "1. **השינויים אצלך, אמא**: תאר את השינויים הפיזיים והרגשיים הנפוצים בשבוע זה. **חובה להשתמש בכוכביות בולטות (*) לכל שינוי או תסמין. לדוגמה: * בחילות מוגברות.**\n" +
                            "2. **איך להתמודד ולהתכונן**: ספק עצות מעשיות להתמודדות עם תסמינים והכנות שרלוונטיות לשלב זה של ההריון (בדיקות, תזונה). **חובה לכתוב סעיף זה כפסקה רציפה אחת בלבד, המורכבת ממשפטים מלאים וזורמים. אסור בתכלית האיסור להשתמש בסימני רשימה, תבליטים, כוכביות, מקפים או כל צורת פיצול אחרת.**\n" +
                            "3. **טיפ זוגי לשבוע זה**: ספק פעילות משותפת קצרה או עצה לחיזוק הקשר הזוגי בשבוע זה. **חובה לכתוב סעיף זה כפסקה רציפה אחת בלבד, המורכבת ממשפטים מלאים וזורמים. אסור בתכלית האיסור להשתמש בסימני רשימה, תבליטים, כוכביות, מקפים או כל צורת פיצול אחרת.**\n" +
                            "המידע חייב להיות חיובי, מעודד וספציפי לשבוע ההריון.",
                    week,
                    days
            );
        }
        else {
             prompt = String.format(
                    "ספק הנחיות ממוקדות לבן/בת הזוג (האב) לתמיכה באם בשבוע %d. " +
                            "**חובה לחלק את התשובה לשלושה סעיפים מדויקים באמצעות כותרות בולטות (Markdown headers) בלבד.** " +
                            "שלושת הסעיפים הם:\n" +
                            "1. **תפקידך המרכזי בשבוע זה**: תאר מה הדרך הטובה ביותר לתמוך באמא מבחינה מעשית ורגשית. **השתמש בכוכביות בולטות (*) לכל נקודה מרכזית. לדוגמה: * שאל את האמא מה היא צריכה.**\n" +
                            "2. **כיצד לתמוך באמא**: ספק רעיונות ספציפיים לתמיכה בתסמינים הנפוצים בשבוע זה (למשל, הקלה על בחילות, הכנת אוכל). **חובה לכתוב סעיף זה כפסקה רציפה אחת. אסור שיופיעו בו אף סימני רשימה, תבליטים, קווים או נקודות כלשהם.**\n" + // <--- דרישה לפסקה רציפה
                            "3. **טיפ זוגי לשבוע זה**: ספק פעילות משותפת קצרה או עצה לחיזוק הקשר הזוגי בשבוע זה. **חובה לכתוב סעיף זה כפסקה רציפה אחת. אסור שיופיעו בו אף סימני רשימה, תבליטים, קווים או נקודות כלשהם.**\n" + // <--- דרישה לפסקה רציפה
                            "המידע חייב להיות חיובי, מעודד וספציפי לשבוע ההריון.",
                    week
            );
        }
        tvTitle.setText(String.format(" טוען נתונים עבור %s..." , role.equals("Mom") ? "האם" : "האב"));

        // שימוש בפונקציה הקיימת שלך לשליחת בקשה ל-Gemini
        startGeminiLoading(prompt);

        return v;
    }
    private void updateUIForRole(String role) {
        String[] currentHeaders = role.equals("Mom") ? HEADERS_MOM : HEADERS_DAD;

        if (role.equals("Mom")) {
            tvTitle.setText(String.format("שבוע %d: הריון, הגוף והרגש", week));
        } else { // Dad
            tvTitle.setText(String.format("שבוע %d: תמיכה והכנה לבן/בת הזוג", week));
        }

        tvSection1Header.setText(currentHeaders[0].replaceAll("\\*\\*", ""));
        tvSection2Header.setText(currentHeaders[1].replaceAll("\\*\\*", ""));
        // tvSection3Header לא צריך עדכון כי הוא קבוע ("טיפ זוגי לשבוע זה")
    }


    private void startGeminiLoading(String prompt) {
        Log.d("PARTNER_FRAG","Sending Prompt: " + prompt);
        new GeminiPrompt(prompt, this);
    }

    // ********* יישום ממשק GeminiResponseListener *********

    @Override
    public void onGeminiSuccess(String rawResponse) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    parseAndDisplaySections(rawResponse, role);
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
            });
        }
    }

    @Override
    public void onGeminiFailure(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.e("PARTNER_FRAG", "API Error: " + errorMessage);
                if(errorMessage.contains("Quota exceeded"))
                {
                    tvTitle.setText("הגעת למגבלת השימוש היומית. ניתן להמשיך מחר.");
                }
                else {
                    tvTitle.setText("שגיאת רשת/API. נסה שוב.");
                }
            });
        }
    }


    // ********* לוגיקת ה-Parsing (מותאמת מהקוד הקיים) *********
    private void parseAndDisplaySections(String rawText, String role) {
        String[] currentHeaders = role.equals("Mom") ? HEADERS_MOM : HEADERS_DAD;
        Map<String, String> sections = new HashMap<>();

        // 1. לולאת ה-Parsing לחילוץ המקטעים
        for (int i = 0; i < currentHeaders.length; i++) {
            String currentHeader = currentHeaders[i];
            String nextHeader = (i + 1 < currentHeaders.length) ? currentHeaders[i+1] : null;

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
                // שימוש בפונקציית הניקוי המשותפת (או יצירת אחת ספציפית אם צריך)
                String finalCleanText = cleanRawText(cleanText);
                sections.put(currentHeader, finalCleanText);
            }
        }
        // 2. עדכון ה-UI

        // מקטע 1: שינויים/תפקיד
        String content1 = sections.getOrDefault(currentHeaders[0], "לא נמצא מידע.");
        tvSection1Content.setText(android.text.Html.fromHtml(content1, android.text.Html.FROM_HTML_MODE_LEGACY));

        // מקטע 2: התמודדות/תמיכה
        String content2 = sections.getOrDefault(currentHeaders[1], "לא נמצא מידע.");
        tvSection2Content.setText(android.text.Html.fromHtml(content2, android.text.Html.FROM_HTML_MODE_LEGACY));

        // מקטע 3: טיפ זוגי
        String content3 = sections.getOrDefault(currentHeaders[2], "לא נמצא טיפ זוגי.");
        tvSection3Content.setText(android.text.Html.fromHtml(content3, android.text.Html.FROM_HTML_MODE_LEGACY));
    }

    // ********* פונקציית הניקוי המשותפת (כפי שתוקנה לאחרונה) *********

    private String cleanRawText(String rawDevelopmentText) {
        if (rawDevelopmentText == null || rawDevelopmentText.isEmpty()) {
            return "אין מידע זמין.";
        }

        // 1. הסר את כל המופעים של '###' ואת כל הטקסט שאחריהם (ניקוי סוף התגובה)
        String cleaned = rawDevelopmentText.replaceAll("###.*", "").trim();

        // 2. החלף בולטים (כוכביות או מקפים) בשבירת שורה כפולה (<br>• )
        cleaned = cleaned.replaceAll("[*\\-][\\s*]", "<br>• ");

        // 3. החלף כותרות משנה מודגשות ע"י הוספת שבירת שורה לפניהן והדגשה
        // הוספנו <br><br> כדי ליצור הפרדה ויזואלית טובה יותר
        cleaned = cleaned.replaceAll("(\\*\\*[^\\*]+\\*\\*)", "<br><br><b>$1</b>");

        // 4. סיום ניקוי: הסר את תגי ה-** סביב הכותרות שנותרו
        cleaned = cleaned.replaceAll("\\*\\*", "");

        // 5. הסר את שבירת השורה אם נוצרה בתחילת הטקסט
        if (cleaned.startsWith("<br>")) {
            cleaned = cleaned.replaceFirst("<br>", "").trim();
        }

        return cleaned;
    }
}