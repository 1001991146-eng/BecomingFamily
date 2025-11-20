package com.example.becomingfamily;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WeeklyUpdateActivity extends AppCompatActivity {
    private MyBabyFragment babyFragment;
    private UserSettingsFragment userSettingsFragment;
    private TestsFragment testsFragment;
    private YouFragment youFragment;
    private Button btn_growth, btn_my_life,btn_tests,btn_user_settings;
    private FirebaseDatabase database;
    private DatabaseReference userRef; // A reference to the root or a specific path
    public User user;
    private int week;
    private int days;
    private TextView tvCurrentWeekHeader;
    private ProgressBar progressBarHorizontal;
    public static final String BABY_FRAGMENT_TAG = "my_baby_fragment"; // תג קבוע
    public static final String YOU_FRAGMENT_TAG = "you_baby_fragment"; // תג קבוע
    public static final String TESTS_FRAGMENT_TAG = "tests_baby_fragment"; // תג קבוע
    public static final String SETTINGS_FRAGMENT_TAG = "settings_baby_fragment"; // תג קבוע

    public void init()
    {
        btn_growth=findViewById(R.id.btn_growth);
        btn_my_life=findViewById(R.id.btn_my_life);
        btn_tests=findViewById(R.id.btn_tests);
        btn_user_settings=findViewById(R.id.btn_user_settings);
        tvCurrentWeekHeader=findViewById(R.id.tvCurrentWeekHeader);
        progressBarHorizontal=findViewById(R.id.progressBarHorizontal);

        calculateCurrentWeek();
        tvCurrentWeekHeader.setText("שבוע "+Integer.toString(week)+" ( יום "+days+" )");
        progressBarHorizontal.setProgress(week);

    }
    public void calculateCurrentWeek() {
         user=new User();
        Log.d("MARIELA","calculateCurrentWeek");
        // נניח ש-user הוא אובייקט User המעודכן
        if (user == null || user.getLastPeriodDate() == null) {
            return ; // טיפול במקרה של נתונים חסרים
        }
        LastPeriodDate last = user.getLastPeriodDate();
        // 1. יצירת אובייקט Calendar
        Calendar lastPeriodCal = Calendar.getInstance();
        // 2. הגדרת התאריך הנכון:
        // הערה חשובה: חודשי Calendar הם 0-11, לכן יש להחסיר 1 ממה שמגיע מה-DB (1-12)
        int year = last.getYear();
        int month = last.getMonth() - 1; // התיקון העיקרי! אם החודש 1 (ינואר), הוא יהפוך ל-0.
        int day = last.getDay();
        // הגדרת התאריך ב-Calendar
        lastPeriodCal.set(year, month, day);
        // 3. חילוץ תאריכים
        Date lastPeriodDate = lastPeriodCal.getTime();
        Date today = new Date(); // התאריך הנוכחי
        // 4. חישוב ההפרש במילישניות ובימים
        long diffInMillies = today.getTime() - lastPeriodDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        // 5. חישוב שבוע ההיריון
        // כל 7 ימים הם שבוע שלם, מוסיפים 1 כדי להתחיל ספירה משבוע 1.
        week= (int) (diffInDays / 7) + 1;
        days=(int) (diffInDays % 7);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weekly_update);
        Log.d("MARIELA","WeeklyUpdateActivity");

        init();
        babyFragment=new MyBabyFragment(WeeklyUpdateActivity.this,week);
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, babyFragment);
        ft.addToBackStack(BABY_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
        ft.commit();


        btn_growth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                babyFragment=new MyBabyFragment(WeeklyUpdateActivity.this,week);
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, babyFragment);
                ft.addToBackStack(BABY_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
                ft.commit();
            }
        });
        btn_my_life.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youFragment=new YouFragment(WeeklyUpdateActivity.this,week,user.getRole());
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, youFragment);
                ft.addToBackStack(YOU_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
                ft.commit();
            }
        });
        btn_tests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testsFragment=new TestsFragment(WeeklyUpdateActivity.this,week);
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, testsFragment);
                ft.addToBackStack(TESTS_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
                ft.commit();
            }
        });
        btn_user_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSettingsFragment=new UserSettingsFragment(WeeklyUpdateActivity.this,week);
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, userSettingsFragment);
                ft.addToBackStack(SETTINGS_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
                ft.commit();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.weelkyUpdate), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
}