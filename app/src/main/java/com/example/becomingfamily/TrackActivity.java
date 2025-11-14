package com.example.becomingfamily;

import android.app.DatePickerDialog;
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
private TextView tv_selected_date,tvEstematedDate;
private Button btn_calculate;
private Calendar calendar;
private FirebaseDatabase database;
private DatabaseReference userRef; // A reference to the root or a specific path//
private User user;

public  void init()
{
    tv_selected_date=findViewById(R.id.tv_selected_date);
    btn_calculate=findViewById(R.id.btn_calculate);
    tvEstematedDate=findViewById(R.id.tvEstematedDate);
    calendar = Calendar.getInstance();
    tvEstematedDate.setText("");
    database = FirebaseDatabase.getInstance();
    userRef = database.getReference("Users");
    user=new User();
}
private void showDatePickerDialog() {
        // יצירת מופע של DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                TrackActivity.this, // הקונטקסט (Activity)

                // המאזין (Listener) לתאריך שנבחר
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // עדכון אובייקט ה-Calendar עם התאריך החדש
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // קריאה לפונקציה המעדכנת את התצוגה
                        updateDateInView();
                    }
                },

                // ערכי ברירת המחדל להתחלה (שנה, חודש, יום)
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // הגבלת המקסימום תאריך ליום הנוכחי (כדי למנוע בחירה עתידית)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // הצגת הדיאלוג
        datePickerDialog.show();
    }
    private void updateDateInView() {
        // הגדרת הפורמט הרצוי: DD/MM/YYYY
        String dateFormat = "dd/MM/yyyy";

        // יצירת SimpleDateFormat עם פורמט ומדיניות אנגלית (US)
        // (או עברית Locale("he") אם רוצים פורמט מילולי)
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);

        // הצבת התאריך המעוצב ב-TextView
        tv_selected_date.setText(sdf.format(calendar.getTime()));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_track);
        init();
        tv_selected_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        btn_calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvEstematedDate.setText(calculatePregnancyDetails(tv_selected_date));
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.llTrack), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private String calculatePregnancyDetails(TextView tvSelectedDate) {
        String lastPeriodDateString = tvSelectedDate.getText().toString();

        // ודא שנבחר תאריך
        if (lastPeriodDateString.isEmpty() || lastPeriodDateString.equals("בחרי תאריך")) {
            return "יש לבחור תחילה את תאריך המחזור האחרון.";
        }

        // הגדרת הפורמט של התאריך
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        try {
            // 1. המרת המחרוזת לתאריך
            Date lastPeriodDate = sdf.parse(lastPeriodDateString);
            Date today = new Date(); // תאריך נוכחי

            if (lastPeriodDate == null || lastPeriodDate.after(today)) {
                return "התאריך שנבחר אינו תקין (ייתכן שהוא עתידי).";
            }

            // 2. חישוב מספר הימים שעברו
            long diffInMillies = today.getTime() - lastPeriodDate.getTime();
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            // 3. חישוב שבוע ההיריון (חלוקה ב-7)
            int currentWeek = (int) (diffInDays / 7) + 1; // +1 מכיוון ששבוע 0 הוא השבוע הראשון
            int daysIntoWeek = (int) (diffInDays % 7);

            // 4. חישוב תאריך הלידה המשוער (Naegele's Rule: +280 ימים)
            Calendar cal = Calendar.getInstance();
            cal.setTime(lastPeriodDate);
            cal.add(Calendar.DAY_OF_YEAR, 280); // 40 שבועות * 7 ימים

            Date estimatedDueDate = cal.getTime();
            String estimatedDateString = sdf.format(estimatedDueDate);
            UpdateUser(lastPeriodDate,estimatedDueDate);

            // 5. הרכבת התוצאה
            return String.format(
                    Locale.getDefault(),
                    "שבוע ההיריון הנוכחי: **%d + %d ימים**\nתאריך הלידה המשוער (EDD): **%s**",
                    currentWeek,
                    daysIntoWeek,
                    estimatedDateString
            );

        } catch (Exception e) {
            e.printStackTrace();
            return "אירעה שגיאה בחישוב התאריכים.";
        }
    }
    public void UpdateUser(Date lastPeriodDate, Date estimatedDate)
    {
    Log.d("MARIELA","Update dates "+lastPeriodDate.toString()+","+estimatedDate.toString());
        lastPeriodDate.setMonth(lastPeriodDate.getMonth()+1);
        estimatedDate.setMonth(estimatedDate.getMonth()+1);
        user.setLastPeriodDate(lastPeriodDate);
        user.setEstimatedDate(estimatedDate);
        userRef.orderByChild("email").equalTo(user.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // נמצא משתמש עם האימייל הנתון
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // שלב 2: עדכון הנתונים של המשתמש
                                snapshot.getRef().setValue(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("MARIELA", "User properties for " + user.getEmail() + " updated successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("MARIELA", "Failed to update user properties for " + user.getEmail(), e);
                                        });
                            }
                        } else {
                            // לא נמצא משתמש עם האימייל הנתון
                            Log.d("MARIELA", "User with email " + user.getEmail() + " not found for update.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MARIELA", "Database query cancelled: " + error.getMessage());

                    }

                });


    }
}