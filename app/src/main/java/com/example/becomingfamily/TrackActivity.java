package com.example.becomingfamily;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TrackActivity extends AppCompatActivity {
    private TextView tv_selected_date, tvEstematedDate;
    private Calendar calendar; // תאריך הוסת האחרונה שנבחר
    private Button btn_LetsGo;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private User user;

    public void init() {
        tv_selected_date = findViewById(R.id.tv_selected_date);
        tvEstematedDate = findViewById(R.id.tvEstematedDate);
        btn_LetsGo = findViewById(R.id.btn_LetsGo);
        calendar = Calendar.getInstance();
        tvEstematedDate.setText("");
        
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        user = UserManager.getInstance();

        if (user == null) {
            Log.e("MARIELA", "User is null in TrackActivity");
            return;
        }
    }

    public void processLastPeriodDate(User user) {
        if (user == null || user.getLastPeriodDate() == null) {
            calendar = Calendar.getInstance();
            return;
        }
        
        LastPeriodDate lpd = user.getLastPeriodDate();
        if (lpd.getYear() > 2000) {
            calendar.set(lpd.getYear(), lpd.getMonth() - 1, lpd.getDay());
            updateDateInView();
            tvEstematedDate.setText(calculatePregnancyDetails());
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                TrackActivity.this,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    updateDateInView();
                    tvEstematedDate.setText(calculatePregnancyDetails());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDateInView() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        tv_selected_date.setText(sdf.format(calendar.getTime()));
    }

    /**
     * מבצע את החישוב תוך שימוש במחלקת השירות WeekCalculator למניעת כפילות קוד (Code Smell)
     */
    private String calculatePregnancyDetails() {
        Date selectedDate = calendar.getTime();
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        if (selectedDate.after(today)) {
            return "התאריך שנבחר אינו תקין (עתידי).";
        }

        // --- שימוש ב-WeekCalculator ---
        WeekCalculator weekCalc = new WeekCalculator(calendar.getTimeInMillis());
        int currentWeek = weekCalc.getWeek();
        int daysIntoWeek = weekCalc.getDays();
        
        saveCurrentData(currentWeek);

        // חישוב תאריך לידה משוער (EDD)
        Calendar eddCal = (Calendar) calendar.clone();
        eddCal.add(Calendar.DAY_OF_YEAR, 280);
        String estimatedDateString = sdf.format(eddCal.getTime());

        // יצירת אובייקטים לעדכון
        LastPeriodDate lpdToSave = new LastPeriodDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
        );
        EstimatedDate edToSave = new EstimatedDate(
                eddCal.get(Calendar.DAY_OF_MONTH),
                eddCal.get(Calendar.MONTH) + 1,
                eddCal.get(Calendar.YEAR)
        );

        updateUserInFirebase(lpdToSave, edToSave);
        saveLMPDateToPrefs(calendar.getTimeInMillis());

        return String.format(
                Locale.getDefault(),
                "שבוע ההיריון הנוכחי: **%d + %d ימים**\nתאריך הלידה המשוער: **%s**",
                currentWeek,
                daysIntoWeek,
                estimatedDateString
        );
    }

    private void saveCurrentData(int week) {
        SharedPreferences sp = getSharedPreferences(MyConstants.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        sp.edit().putInt(MyConstants.KEY_WEEKS, week).apply();
    }

    private void saveLMPDateToPrefs(long lmpMillis) {
        SharedPreferences sp = getSharedPreferences(MyConstants.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        sp.edit().putLong(MyConstants.KEY_LMP_DATE, lmpMillis).apply();
    }

    private void updateUserInFirebase(LastPeriodDate lpd, EstimatedDate ed) {
        userRef.orderByChild("email").equalTo(user.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User u = ds.getValue(User.class);
                            if (u != null) {
                                u.setLastPeriodDate(lpd);
                                u.setEstimatedDate(ed);
                                UserManager.setInstance(u);
                                ds.getRef().setValue(u);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_track);
        init();

        User u = UserManager.getInstance();
        if (u != null) {
            processLastPeriodDate(u);
        } else {
            calendar = Calendar.getInstance();
        }

        tv_selected_date.setOnClickListener(v -> showDatePickerDialog());
        btn_LetsGo.setOnClickListener(v -> {
            if (UserManager.getInstance().getLastPeriodDate() != null) {
                startActivity(new Intent(this, WeeklyUpdateActivity.class));
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.llTrack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
