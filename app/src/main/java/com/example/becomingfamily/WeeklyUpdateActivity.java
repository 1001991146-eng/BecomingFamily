package com.example.becomingfamily;

import androidx.appcompat.app.AlertDialog;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class WeeklyUpdateActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {
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
    private ConnectivityReceiver connectivityReceiver;
    private AlertDialog noConnectionDialog;
// --- מתודת עזר סטטית לבדיקת רשת ---
    /**
     * בודק אם יש חיבור רשת פעיל.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // מחזיר True אם הרשת פעילה או בתהליך התחברות
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
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
         user=UserManager.getInstance();

        Log.d("MARIELA","calculateCurrentWeek");
        // נניח ש-user הוא אובייקט User המעודכן
        if (user == null || user.getLastPeriodDate() == null) {
            Log.e("MARIELA", "User is null in calculateCurrentWeek");
            week=1;
            days=1;
            return ; // טיפול במקרה של נתונים חסרים
        }
        LastPeriodDate last = UserManager.getInstance().getLastPeriodDate();
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
    public void scheduleReminderJob() {
        // ID ייחודי לעבודה שלך
        int jobId = 101;

        // מכינים את ה-JobInfo
        ComponentName serviceComponent = new ComponentName(this, MyReminderJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);

        // *הגדרת החזרתיות:*
        // JobScheduler לא תומך ב"הפעלה פעם בחודש".
        // הפתרון המקובל הוא להשתמש ב-setPeriodic עם הזמן המינימלי
        // (למשל, 24 שעות) ואז **בתוך ה-JobService** לבדוק אם עבר חודש!

        // נניח, הפעלה כל 24 שעות (86400000 מילישניות):
        long intervalMillis = 24 * 60 * 60 * 1000L;
        builder.setPeriodic(intervalMillis);

        // הגדרות נוספות מומלצות:
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // אפשר להגדיר תנאים
        builder.setPersisted(true); // אם המכשיר אתחל מחדש, ה-Job יישמר.

        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        int result = jobScheduler.schedule(builder.build());

        if (result == JobScheduler.RESULT_SUCCESS) {
            // סמיילי פייס: "המשימה נכנסה לתור בהצלחה! 💪"
            Log.d("MARIELA","Scheduling service succeded");

        } else {
            // המבאס: "איזה באסה, משהו נכשל ב-Scheduling..."
            Log.d("MARIELA","Scheduling service failed");
        }
    }
    public long getLPD() {
        Calendar calendar = Calendar.getInstance();
        User user=UserManager.getInstance();

        // שימו לב: חודשים ב-Calendar מתחילים מ-0 (0=ינואר, 11=דצמבר)
        // לכן אנחנו מפחיתים 1 מהחודש ששמרתם.
        calendar.set(UserManager.getInstance().getLastPeriodDate().getYear(), UserManager.getInstance().getLastPeriodDate().getMonth() - 1, UserManager.getInstance().getLastPeriodDate().getDay(), 0, 0, 0); // 0,0,0 = חצות

        return calendar.getTimeInMillis();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weekly_update);
        Log.d("MARIELA","WeeklyUpdateActivity");

        init();
        scheduleReminderJob();
        connectivityReceiver = new ConnectivityReceiver();

        babyFragment=new MyBabyFragment(WeeklyUpdateActivity.this,week,days);
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, babyFragment);
        ft.addToBackStack(BABY_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
        ft.commit();
        Log.d("MARIELA","Save baby fragment tag");


        btn_growth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                babyFragment=new MyBabyFragment(WeeklyUpdateActivity.this,week,days);
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, babyFragment);
                ft.addToBackStack(BABY_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
                ft.commit();
                Log.d("MARIELA","Save baby fragment tag");
            }
        });
        btn_my_life.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youFragment=new YouFragment(WeeklyUpdateActivity.this,week,days,UserManager.getInstance().getRole());
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, youFragment);
                ft.addToBackStack(YOU_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
                ft.commit();
                Log.d("MARIELA","Save you fragment tag");
            }
        });
        btn_tests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testsFragment=new TestsFragment(WeeklyUpdateActivity.this,week,days);
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, testsFragment);
                ft.addToBackStack(TESTS_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
                ft.commit();
                Log.d("MARIELA","Save tests fragment tag");
            }
        });
        btn_user_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSettingsFragment=new UserSettingsFragment(WeeklyUpdateActivity.this,week,days);
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, userSettingsFragment);
                ft.addToBackStack(SETTINGS_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
                ft.commit();
                Log.d("MARIELA","Save user settings fragment tag");
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.weelkyUpdate), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    // --- ניהול ה-BroadcastReceiver (רישום וביטול רישום) ---

    @Override
    protected void onResume() {
        super.onResume();

        // **5. רישום:** ה-Activity מוגדר כמאזין וה-Receiver נרשם להאזנה לשינויי רשת
        ConnectivityReceiver.connectivityReceiverListener = WeeklyUpdateActivity.this;
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        // שימוש ב-registerReceiver עם IntentFilter
        registerReceiver(connectivityReceiver, intentFilter);

        // בדיקה מיידית של המצב הנוכחי עם טעינת האקטיביטי
        handleConnectivityChange(isNetworkAvailable(this));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // **6. ביטול רישום:** ביטול הרישום של ה-Receiver למניעת דליפות זיכרון
        unregisterReceiver(connectivityReceiver);
        ConnectivityReceiver.connectivityReceiverListener = null;
    }

    /**
     * יישום המתודה מהממשק לטיפול בשינוי מצב רשת, נקרא על ידי ה-Receiver
     * @param isConnected True אם מחובר, False אחרת.
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        handleConnectivityChange(isConnected);
    }

    /**
     * לוגיקה מרכזית לטיפול בחיבור/ניתוק
     */
    private void handleConnectivityChange(boolean isConnected) {
        if (!isConnected) {
            // טיפול במקרה של ניתוק
            Log.d("NetworkStatus", "Internet Disconnected!");
            showNoConnectionDialog();
        } else {
            // טיפול במקרה של חיבור מחדש
            Log.d("NetworkStatus", "Internet Connected!");

            // סגירת הדיאלוג אם הוא מוצג
            if (noConnectionDialog != null && noConnectionDialog.isShowing()) {
                noConnectionDialog.dismiss();
                noConnectionDialog = null;
            }
            Toast.makeText(this, "הרשת חזרה! הנתונים מעודכנים כעת.", Toast.LENGTH_SHORT).show();
            babyFragment=new MyBabyFragment(WeeklyUpdateActivity.this,week,days);
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, babyFragment);
            ft.addToBackStack(BABY_FRAGMENT_TAG); // הוספת התג כ'שם' לערימה
            ft.commit();
        }
    }

    /**
     * מציג דיאלוג אזהרה מונע סגירה על ניתוק רשת
     */
    private void showNoConnectionDialog() {
        // מציג דיאלוג רק אם הוא אינו מוצג כבר
        if (noConnectionDialog != null && noConnectionDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ אין חיבור לאינטרנט");
        builder.setMessage("האפליקציה דורשת חיבור רשת פעיל. נתונים חדשים או עדכונים לא ייטענו עד שתתחברי שוב.");
        // ניתן להשתמש באייקון רלוונטי אם יש (לדוגמה: R.drawable.ic_no_internet)
        // builder.setIcon(R.drawable.baby);

        builder.setPositiveButton("הבנתי", null);
        builder.setCancelable(false); // מונע סגירה על ידי לחיצה מחוץ לדיאלוג (חובה במקרה של חוסר רשת)

        noConnectionDialog = builder.create();
        noConnectionDialog.show();
    }
}