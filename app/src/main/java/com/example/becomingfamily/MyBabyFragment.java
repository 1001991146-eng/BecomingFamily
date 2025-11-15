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
import java.util.concurrent.TimeUnit;

import kotlin.LateinitKt;


public class MyBabyFragment extends Fragment implements GeminiResponseListener {
    private  int week;
    private Activity activity;
    private TextView tvBabySize;
    private TextView tvGeminiInfo;
    private ImageView ivBabyImage;


    private TextView tv_Baby_Weeks;
    public MyBabyFragment(Activity activity, int week) {
        // Required empty public constructor
        this.week=week;
        this.activity=activity;
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
                    // 1. ביצוע ה-Parsing ב-Fragment
                    ParsedData parsed = splitGeminiResponse(rawResponse);

                    // 2. עדכון ה-UI
                    tvBabySize.setText(parsed.getSizeText());
                    tvGeminiInfo.setText(android.text.Html.fromHtml(parsed.getOtherInfo(), android.text.Html.FROM_HTML_MODE_LEGACY));

                } catch (Exception e) {
                    tvGeminiInfo.setText("שגיאה בעיבוד התוכן.");
                }
            });
        }
    }

    @Override
    public void onGeminiFailure(String errorMessage) {
        Log.d("MARIELA","onGeminiFailure!!!");

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvGeminiInfo.setText("שגיאת רשת/API: " + errorMessage);
            });
        }
    }
    // ב. לוגיקת ה-Parsing (ניתן להעביר אותה למחלקה סטטית נפרדת אם תרצה)
    private ParsedData splitGeminiResponse(String rawText) {
        Log.d("MARIELA","splitGeminiResponse "+rawText);

        final String SIZE_HEADER = "גודל והשוואה";
        final String NEXT_WEEK_HEADER = "מה צפוי בשבוע הבא?";

        int sizeStartIndex = rawText.indexOf(SIZE_HEADER);
        int nextSectionIndex = rawText.indexOf(NEXT_WEEK_HEADER);

        String cleanSizeText = "";
        String allOtherText = rawText; // ברירת מחדל: כל הטקסט

        if (sizeStartIndex != -1) {
            if (nextSectionIndex != -1 && nextSectionIndex > sizeStartIndex) {
                // חילוץ טקסט הגודל והצגתו ב-tvSizeInfo
                String sizeSection = rawText.substring(sizeStartIndex, nextSectionIndex).trim();
                cleanSizeText = sizeSection.replace(SIZE_HEADER, "").trim();
            }
        }

        // ... ניתן להוסיף לוגיקה מורכבת יותר כאן ...

        return new ParsedData(cleanSizeText, allOtherText);
    }
    // מחלקה פנימית להחזרת נתונים מפוצלים
    private static class ParsedData {
        private final String sizeText;
        private final String otherInfo;

        public ParsedData(String sizeText, String otherInfo) {
            this.sizeText = sizeText;
            this.otherInfo = otherInfo;
        }

        public String getSizeText() { return sizeText; }
        public String getOtherInfo() { return otherInfo; }
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
        tvGeminiInfo=v.findViewById(R.id.tvGeminiInfo);



        Log.d("MARIELA","current week: "+Integer.toString(week));
        String prompt = String.format(
                "אנא ספק סיכום מקיף ומעודד על התפתחות העובר וגוף האם בשבוע %d. " +
                        "ענה על שלושת הסעיפים הבאים בפירוט, תוך שימוש בכותרות ברורות:\n\n" +
                        "1. התפתחות העובר השבוע: מהם האיברים שהתפתחו, או השינויים המרכזיים שחלו בעובר בשבוע זה? (כותרת: 'התינוק שלך בשבוע %d').\n" +
                        "2. גודל העובר: מהו גודלו של העובר במונחי משקל ואורך (בערך)? השווה את גודלו לפרי נפוץ אחד. (כותרת: 'גודל והשוואה').\n" +
                        "3. למה לצפות בשבוע הבא: מהם השינויים האפשריים בגוף האם ובעובר בשבוע הריון %d? (כותרת: 'מה צפוי בשבוע הבא?').\n",
                week, week, week + 1);
        //GeminiPrompt geminiPrompt=new GeminiPrompt(activity,prompt,tv_Baby_Weeks);
        tv_Baby_Weeks.setText("טוען נתונים...");
        startGeminiLoading(prompt);

        return  v;

    }

}