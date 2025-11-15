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


public class MyBabyFragment extends Fragment {
    private  int week;
    private Activity activity;

    private TextView tv_Baby_Weeks;
    public MyBabyFragment(Activity activity, int week) {
        // Required empty public constructor
        this.week=week;
        this.activity=activity;
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


        Log.d("MARIELA","current week: "+Integer.toString(week));
        String prompt = String.format(
                "אנא ספק סיכום מקיף ומעודד על התפתחות העובר וגוף האם בשבוע %d. " +
                        "ענה על שלושת הסעיפים הבאים בפירוט, תוך שימוש בכותרות ברורות:\n\n" +
                        "1. התפתחות העובר השבוע: מהם האיברים שהתפתחו, או השינויים המרכזיים שחלו בעובר בשבוע זה? (כותרת: 'התינוק שלך בשבוע %d').\n" +
                        "2. גודל העובר: מהו גודלו של העובר במונחי משקל ואורך (בערך)? השווה את גודלו לפרי נפוץ אחד. (כותרת: 'גודל והשוואה').\n" +
                        "3. למה לצפות בשבוע הבא: מהם השינויים האפשריים בגוף האם ובעובר בשבוע הריון %d? (כותרת: 'מה צפוי בשבוע הבא?').\n",
                week, week, week + 1);
        GeminiPrompt geminiPrompt=new GeminiPrompt(activity,prompt,tv_Baby_Weeks);
        tv_Baby_Weeks.setText("טוען נתונים...");
        return  v;

    }

}