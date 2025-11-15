package com.example.becomingfamily;

import android.app.DatePickerDialog;
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
import java.util.concurrent.TimeUnit;

public class TrackActivity extends AppCompatActivity {
    private TextView tv_selected_date, tvEstematedDate;
    private Calendar calendar; // משתנה גלובלי לניהול תאריך המחזור האחרון
    private Button btn_LetsGo;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private User user;

    /* פונקציות עזר להמרת חודש (Firebase: 1-12, Calendar: 0-11) */
    private int toCalendarMonth(int firebaseMonth) {
        return firebaseMonth - 1;
    }

    private int toFirebaseMonth(int calendarMonth) {
        return calendarMonth + 1;
    }

    /* init elements on activity*/
    public void init() {
        tv_selected_date = findViewById(R.id.tv_selected_date);
        tvEstematedDate = findViewById(R.id.tvEstematedDate);
        btn_LetsGo=findViewById(R.id.btn_LetsGo);
        calendar = Calendar.getInstance();
        tvEstematedDate.setText("");
        // connect to firebase
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        user = new User(); // ודא שאתה ממלא את שדה ה-email של user לפני השימוש ב-UpdateUser
        Log.d("MARIELA", "user:" + user.toString());
    }

    /**
     * טוען את תאריך המחזור האחרון מ-Firebase ומעדכן את המשתנה calendar הגלובלי ואת ה-UI.
     */
    public void processLastPeriodDate(User user) {
        Log.d("MARIELA", "processLastPeriodDate");
        // נניח ש-getLastPeriodDate מחזיר LastPeriodDate{day=X, month=Y, year=Z}
        LastPeriodDate lastPeriodDate = user.getLastPeriodDate();

        if (lastPeriodDate != null && lastPeriodDate.getYear() > 2000) { // ודא שיש נתונים הגיוניים
            try {
                // 1. איפוס ועדכון משתנה ה-calendar הגלובלי
                calendar.set(
                        lastPeriodDate.getYear(),
                        toCalendarMonth(lastPeriodDate.getMonth()),
                        lastPeriodDate.getDay()
                );

                // 2. עדכון ה-TextView והצגת החישוב
                updateDateInView();
                // שימו לב: כאן אנו מפעילים את החישוב לאחר הטעינה
                tvEstematedDate.setText(calculatePregnancyDetails());
                Log.d("DATE_SUCCESS", "Calendar set from Firebase.");

            } catch (IllegalArgumentException e) {
                Log.e("DATE_ERROR", "Invalid date values in Firebase: " + e.getMessage());
                // הגדרת ברירת מחדל אם הנתונים ב-Firebase לא תקינים
                tv_selected_date.setText("בחר תאריך");
                calendar = Calendar.getInstance();
            }
        } else {
            Log.e("MARIELA", "LastPeriodDate object is null or invalid. Setting default.");
            calendar = Calendar.getInstance();
            tv_selected_date.setText("בחר תאריך");
        }
    }

    /**
     * מציג את חלונית בוחר התאריך. משתמשת וגם מעדכנת את המשתנה calendar הגלובלי.
     */
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                TrackActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Log.d("MARIELA", "onDateSet");

                        // עדכון המשתנה ה-calendar הגלובלי
                        calendar.set(year, monthOfYear, dayOfMonth);

                        // 1. עדכון ה-TextView
                        updateDateInView();

                        // 2. הפעלת החישוב ועדכון Firebase
                        tvEstematedDate.setText(calculatePregnancyDetails());
                    }
                },
                // ערכי ברירת המחדל להתחלה (מה-calendar הגלובלי)
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDateInView() {
        // הגדרת הפורמט הרצוי: DD/MM/YYYY
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        // הצבת התאריך המעוצב מתוך המשתנה calendar הגלובלי
        tv_selected_date.setText(sdf.format(calendar.getTime()));
    }

    // ----------- חישובים חדשים וממוקדים -----------

    /** מחשב את שבוע ההיריון הנוכחי (משתמש במשתנה calendar הגלובלי) */
    public int calculateCurrentWeek() {
        Date lastPeriodDate = calendar.getTime();
        Date today = new Date();

        long diffInMillies = today.getTime() - lastPeriodDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        return (int) (diffInDays / 7) + 1;
    }

    /** מחשב את מספר הימים לשבוע הבא (משתמש במשתנה calendar הגלובלי) */
    public int calculateDaysIntoWeek() {
        Date lastPeriodDate = calendar.getTime();
        Date today = new Date();

        long diffInMillies = today.getTime() - lastPeriodDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        return (int) (diffInDays % 7);
    }

    /**
     * מבצע את החישוב המלא, מעדכן את Firebase ומחזיר את המחרוזת ל-UI.
     */
    private String calculatePregnancyDetails() {
        Date lastPeriodDate = calendar.getTime();
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        if (lastPeriodDate.after(today)) {
            return "התאריך שנבחר אינו תקין (עתידי).";
        }

        // 1. חישוב שבועות
        int currentWeek = calculateCurrentWeek();
        int daysIntoWeek = calculateDaysIntoWeek();
        SaveCurrentData(currentWeek);

        // 2. חישוב תאריך הלידה המשוער (EDD)
        Calendar eddCal = (Calendar) calendar.clone(); // יצירת עותק של תאריך המחזור האחרון
        eddCal.add(Calendar.DAY_OF_YEAR, 280);

        String estimatedDateString = sdf.format(eddCal.getTime());

        // 3. יצירת אובייקטי שמירה (LastPeriodDate ו-EstimatedDate)
        LastPeriodDate lastPeriodDateToSave = new LastPeriodDate(
                calendar.get(Calendar.DAY_OF_MONTH),
                toFirebaseMonth(calendar.get(Calendar.MONTH)),
                calendar.get(Calendar.YEAR)
        );
        EstimatedDate estimatedDateToSave = new EstimatedDate(
                eddCal.get(Calendar.DAY_OF_MONTH),
                toFirebaseMonth(eddCal.get(Calendar.MONTH)),
                eddCal.get(Calendar.YEAR)
        );

        // 4. עדכון Firebase
        UpdateUser(lastPeriodDateToSave, estimatedDateToSave);

        // 5. הרכבת התוצאה
        return String.format(
                Locale.getDefault(),
                "שבוע ההיריון הנוכחי: **%d + %d ימים**\nתאריך הלידה המשוער (EDD): **%s**",
                currentWeek,
                daysIntoWeek,
                estimatedDateString
        );
    }

    // ... שאר המתודות (SaveCurrentData, UpdateUser) נשארות כפי שהן ...

    public void SaveCurrentData(int week) {
        SharedPreferences sp = getSharedPreferences("BabySteps", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("weeks", week);
        editor.commit();
    }

    public void UpdateUser(LastPeriodDate lastPeriodDate, EstimatedDate estimatedDate) {
        Log.d("MARIELA", "Update dates " + lastPeriodDate.toString() + "," + estimatedDate.toString());

        userRef.orderByChild("email").equalTo(user.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // נמצא משתמש עם האימייל הנתון
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // יש לעדכן את אובייקט ה-user הגלובלי לפני הקריאה ל-setValue:
                                User userToUpdate = snapshot.getValue(User.class);
                                if (userToUpdate != null) {
                                    userToUpdate.setLastPeriodDate(lastPeriodDate);
                                    userToUpdate.setEstimatedDate(estimatedDate);

                                    snapshot.getRef().setValue(userToUpdate) // משתמשים באובייקט המעודכן
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("MARIELA", "User properties updated successfully.");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("MARIELA", "Failed to update user properties.", e);
                                            });
                                }
                            }
                        } else {
                            Log.d("MARIELA", "User not found for update.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MARIELA", "Database query cancelled: " + error.getMessage());
                    }
                });
    }

    /* main */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_track);
        Log.d("MARIELA", "TrackActivity - onCreate");
        init();
        // הערה: קריאה ל-processLastPeriodDate(user) ב-onCreate תלויה בטעינת ה-user.
        // אם ה-user מגיע מ-Intent או SharedPrefs, זה בסדר. אם הוא טרם נטען מה-DB, צריך קוד טעינה.

        // **אם ה-user לא נטען עדיין, יש להוסיף כאן לוגיקת טעינה**
        // לדוגמה: LoadUserAndProcessData();

        processLastPeriodDate(user); // עדיין מניח שה-user מכיל את הנתונים הנחוצים

        tv_selected_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        btn_LetsGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LastPeriodDate lastPeriodDate = user.getLastPeriodDate();
                if (lastPeriodDate!=null)
                {
                    Intent intent=new Intent(TrackActivity.this, WeeklyUpdateActivity.class);
                    startActivity(intent);
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.llTrack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}