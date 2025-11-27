package com.example.becomingfamily;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import kotlin.LateinitKt;


public class MyBabyFragment extends Fragment implements GeminiResponseListener {
    private  int week;
    private int days;
    private Activity activity;
    private TextView tvBabySize;
    private ImageView ivBabyImage;
    private TextView tvDevelopment; // חדש
    private TextView tvWeeklyTip; // חדש

    private TextView tv_Baby_Weeks;
    public MyBabyFragment() {
        // Required empty public constructor
    }
    public MyBabyFragment(Activity activity, int week, int days) {
        // Required empty public constructor
        this.week=week;
        this.activity=activity;
        this.days=days;
    }

    private void startGeminiLoading(String prompt) {
        // ... (הכנת prompt) ...

        // יצירת GeminiPrompt חדש, מעבירים את עצמנו (this) כמאזין
        // אם GeminiPrompt שלך דורש Context, תצטרך להעביר אותו כאן:
        Log.d("MARIELA","Go!!!");
        new GeminiPrompt(prompt, this);
    }
    // ********* שיטת ההצלחה (כאן מתבצע ה-Parsing והצגת ה-UI) *********
    @Override
    public void onGeminiSuccess(String rawResponse) {
        Log.d("MARIELA","onGeminiSuccess!!!");

        // שימוש ב-runOnUiThread כדי לוודא שזה רץ על ה-UI Thread (בטיחות כפולה)
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                 splitGeminiResponse(rawResponse);

                } catch (Exception e) {
                    tv_Baby_Weeks.setText("שגיאה בעיבוד התוכן.");
                }
            });
        }
    }

    @Override
    public void onGeminiFailure(String errorMessage) {

        Log.d("MARIELA","onGeminiFailure!!!");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if(errorMessage.contains("Quota exceeded"))
                {
                    tv_Baby_Weeks.setText("הגעת למגבלת השימוש היומית. ניתן להמשיך מחר.");
                }
                else {
                    tv_Baby_Weeks.setText("שגיאת רשת/API. לא ניתן לטעון מידע .");
                }
            });
        }
    }
    // ב. לוגיקת ה-Parsing (ניתן להעביר אותה למחלקה סטטית נפרדת אם תרצה)
    private void splitGeminiResponse(String rawText) {
        Log.d("MARIELA","splitGeminiResponse "+rawText);
            final String[] HEADERS = {
                    "**גודלו של התינוק**",
                    "**התפתחות העובר בשבוע זה**",
                    "**טיפים שבועיים**" // כותרת חדשה
            };

            Map<String, String> sections = new HashMap<>();

            // *** 1. לולאת ה-Parsing לחילוץ המקטעים ***
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
                    String clean = cleanRawText(cleanText);
                    sections.put(currentHeader, clean);
                }
            }

        String sizeText = sections.getOrDefault(HEADERS[0], "לא נמצא מידע גודל.");
        tvBabySize.setText(android.text.Html.fromHtml(sizeText, android.text.Html.FROM_HTML_MODE_LEGACY));

// דוגמה לחילוץ טקסט ההתפתחות:
        String devText = sections.getOrDefault(HEADERS[1], "לא נמצא מידע התפתחות.");
        tvDevelopment.setText(android.text.Html.fromHtml(devText, android.text.Html.FROM_HTML_MODE_LEGACY));

        String tipText = sections.getOrDefault(HEADERS[2], "לא נמצאו טיפים רלוונטיים.");
        tvWeeklyTip.setText(android.text.Html.fromHtml(tipText,android.text.Html.FROM_HTML_MODE_LEGACY));
        tv_Baby_Weeks.setText("המסע המופלא של התינוק");
    }
    private String cleanRawText(String rawDevelopmentText) {
        if (rawDevelopmentText == null || rawDevelopmentText.isEmpty()) {
            return "אין מידע התפתחות זמין.";
        }

        // 1. נסיר רווחים מיותרים ש-Gemini יכול להוסיף בתחילת השורה
        String cleaned = rawDevelopmentText.trim();

        cleaned = rawDevelopmentText.replaceAll("###.*", "").trim();

        // 2. החלף בולטים (כוכביות או מקפים) בשבירת שורה כפולה (<br><br>)
        //    כדי להפריד ויזואלית כל נקודה (הוספת רווח לפני כדי למנוע הדבקות למילה קודמת)
        //    הביטוי הרגולרי: ([*\\-][\\s*]) יחפש * או - ואחריהם רווח (בכמה מקומות בשורה)
        cleaned = cleaned.replaceAll("[*\\-][\\s*]", "<br>• ");

        // 3. החלף כותרות משנה מודגשות ע"י הוספת שבירת שורה לפניהן
        //    כדי שהן לא יופיעו סמוך מדי לטקסט שמעליהן
        //    הביטוי הרגולרי: (\\*\\*[^\\*]+\\*\\*) יחפש כל טקסט בין ** ל-**
        cleaned = cleaned.replaceAll("(\\*\\*[^\\*]+\\*\\*)", "<br><br><b>$1</b>");

        // 4. סיום ניקוי: הסר את תגי ה-** סביב הכותרות (כדי למנוע כפילות בולטת)
        cleaned = cleaned.replaceAll("\\*\\*", "");

        // 5. הסר את שבירת השורה אם נוצרה בתחילת הטקסט
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_my_baby, container, false);
        tv_Baby_Weeks = v.findViewById(R.id.tv_Baby_Weeks);
        tvBabySize=v.findViewById(R.id.tvBabySize);
        ivBabyImage=v.findViewById(R.id.ivBabyImage);
        tvDevelopment=v.findViewById(R.id.tvDevelopment);
        tvWeeklyTip=v.findViewById(R.id.tvWeeklyTip);


        Log.d("MARIELA","current week: "+Integer.toString(week));

        String prompt = String.format(
                "אתה יועץ הריון, מומחה להתפתחות עוברית, בעל נימה חיובית ומעודדת. ענה במקצועיות והתמקד אך ורק בתינוק."+
                        "ספק סיכום מקיף על התפתחות העובר בשבוע %d יום %d. " +
                        "**חובה לחלק את התשובה לשלושה סעיפים מדויקים באמצעות כותרות בולטות (Markdown headers) בלבד.** " +
                        "שלושת הסעיפים הם:\n" +
                        "1. **גודלו של התינוק**: תאר את גודלו של העובר במונחי משקל ואורך (בערך), והשווה אותו לפרי נפוץ אחד. **חובה לכתוב סעיף זה כפסקה רציפה אחת. אסור שיופיעו בו אף סימני רשימה, תבליטים, קווים או נקודות כלשהם.**\n" +
                        "2. **התפתחות העובר בשבוע זה**: פרט את השינויים הגופניים והתפקודיים המרכזיים שחלים בעובר בשבוע זה. **השתמש בכוכביות בולטות (*) לכל שינוי או איבר מרכזי. לדוגמה: * התפתחות הלב.**\n" + // <--- שינוי קריטי: מעבר לשימוש בכוכביות (*)
                        "3. **טיפים שבועיים**: ספק עצה מעשית קצרה ורלוונטית לשבוע זה (למשל, עצה לתמיכה רגשית או משהו שאפשר לנסות בבית). \n" +
                        "המידע חייב להתמקד אך ורק בעובר ובטיפים כלליים/רלוונטיים לשבוע זה.",
                week,
                days
        );
        //GeminiPrompt geminiPrompt=new GeminiPrompt(activity,prompt,tv_Baby_Weeks);
        tv_Baby_Weeks.setText("טוען נתונים...");
        startGeminiLoading(prompt);

        return  v;

    }

}