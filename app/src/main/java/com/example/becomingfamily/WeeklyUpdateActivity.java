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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

    // Using tags to find fragments later
    public static final String BABY_FRAGMENT_TAG = "my_baby_fragment";
    public static final String YOU_FRAGMENT_TAG = "you_fragment_tag";
    public static final String TESTS_FRAGMENT_TAG = "tests_fragment_tag";
    public static final String SETTINGS_FRAGMENT_TAG = "settings_fragment_tag";

    private ConnectivityReceiver connectivityReceiver;
    private AlertDialog noConnectionDialog;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
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

        if (user == null || user.getLastPeriodDate() == null) {
            week=1;
            days=1;
            return;
        }
        LastPeriodDate last = UserManager.getInstance().getLastPeriodDate();
        Calendar lastPeriodCal = Calendar.getInstance();
        int year = last.getYear();
        int month = last.getMonth() - 1;
        int day = last.getDay();
        lastPeriodCal.set(year, month, day);
        Date lastPeriodDate = lastPeriodCal.getTime();
        Date today = new Date();
        long diffInMillies = today.getTime() - lastPeriodDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        week= (int) (diffInDays / 7) ;
        days=(int) (diffInDays % 7);
    }

    public void scheduleReminderJob() {
        int jobId = 101;
        ComponentName serviceComponent = new ComponentName(this, MyReminderJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        long intervalMillis = 24 * 60 * 60 * 1000L;
        builder.setPeriodic(intervalMillis);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weekly_update);
        init();
        scheduleReminderJob();
        connectivityReceiver = new ConnectivityReceiver();

        // Load the initial fragment only if the activity is newly created
        if (savedInstanceState == null) {
            babyFragment = new MyBabyFragment(WeeklyUpdateActivity.this, week, days);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, babyFragment, BABY_FRAGMENT_TAG)
                    .commit();
        }

        btn_growth.setOnClickListener(v -> {
            if (babyFragment == null) {
                babyFragment = new MyBabyFragment(WeeklyUpdateActivity.this, week, days);
            }
            showFragment(babyFragment, BABY_FRAGMENT_TAG);
        });

        btn_my_life.setOnClickListener(v -> {
            if (youFragment == null) {
                youFragment = new YouFragment(WeeklyUpdateActivity.this, week, days, UserManager.getInstance().getRole());
            }
            showFragment(youFragment, YOU_FRAGMENT_TAG);
        });

        btn_tests.setOnClickListener(v -> {
            if (testsFragment == null) {
                testsFragment = new TestsFragment(WeeklyUpdateActivity.this, week, days);
            }
            showFragment(testsFragment, TESTS_FRAGMENT_TAG);
        });

        btn_user_settings.setOnClickListener(v -> {
            if (userSettingsFragment == null) {
                userSettingsFragment = new UserSettingsFragment(WeeklyUpdateActivity.this, week, days);
            }
            showFragment(userSettingsFragment, SETTINGS_FRAGMENT_TAG);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.weelkyUpdate), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Find if the fragment is already added
        Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (existingFragment != null) {
            // If it exists, just show it
            ft.replace(R.id.fragment_container, existingFragment);
        } else {
            // If not, add it with the tag
            ft.replace(R.id.fragment_container, fragment, tag);
            ft.addToBackStack(tag);
        }
        ft.commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityReceiver.connectivityReceiverListener = this;
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        handleConnectivityChange(isNetworkAvailable(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(connectivityReceiver);
        ConnectivityReceiver.connectivityReceiverListener = null;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        handleConnectivityChange(isConnected);
    }

    private void handleConnectivityChange(boolean isConnected) {
        if (!isConnected) {
            showNoConnectionDialog();
        } else {
            if (noConnectionDialog != null && noConnectionDialog.isShowing()) {
                noConnectionDialog.dismiss();
                noConnectionDialog = null;
            }
        }
    }

    private void showNoConnectionDialog() {
        if (noConnectionDialog != null && noConnectionDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ אין חיבור לאינטרנט");
        builder.setMessage("האפליקציה דורשת חיבור רשת פעיל. נתונים חדשים או עדכונים לא ייטענו עד שתתחברי שוב.");
        builder.setPositiveButton("הבנתי", null);
        builder.setCancelable(false);
        noConnectionDialog = builder.create();
        noConnectionDialog.show();
    }
}