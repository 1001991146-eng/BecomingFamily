package com.example.becomingfamily;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth; // חובה
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private ExtendedFloatingActionButton fabDad, fabMom;
    private TextInputEditText etFullName, etEmailRegister, etRegisterPassword, etConfirmPassword, etMobile;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private String selectedRole = "Mom";
    private FirebaseAuth mAuth; // משתנה תקני
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    public void init() {
        fabDad = findViewById(R.id.fabDad);
        fabMom = findViewById(R.id.fabMom);
        etFullName = findViewById(R.id.etFullName);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmailRegister = findViewById(R.id.etEmailRegister);
        btnRegister = findViewById(R.id.btnRegister);
        etMobile = findViewById(R.id.etMobile);

        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvBackToLogin.setPaintFlags(tvBackToLogin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // עיצוב כפתורים
        fabMom.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.teal_200));
        fabDad.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        selectedRole = "Mom";

        // אתחול Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
    }

    public boolean verifyUser() {
        if (etEmailRegister.getText().toString().trim().isEmpty()) {
            etEmailRegister.setError("Missing email");
            return false;
        }
        if (etMobile.getText().toString().trim().isEmpty()) {
            etMobile.setError("Missing phone");
            return false;
        }
        if (etRegisterPassword.getText().toString().trim().isEmpty()) {
            etRegisterPassword.setError("Missing password");
            return false;
        }
        if (etRegisterPassword.getText().toString().length() < 6) {
            etRegisterPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!etRegisterPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            etConfirmPassword.setError("Password must be the same");
            return false;
        }
        return true;
    }

    // שמירת האימייל לשימוש עתידי (זכור אותי)
    private void saveEmailToPrefs(String email) {
        SharedPreferences sp = getSharedPreferences(MyConstants.SHARED_PREFS_FILE, MODE_PRIVATE);
        sp.edit().putString(MyConstants.KEY_EMAIL, email).apply();
    }

    // שמירת תאריך ה-LMP עבור ה-JobService
    private void saveLMPToPrefs(LastPeriodDate lpd) {
        Calendar cal = Calendar.getInstance();
        // תיקון חודש (0-11)
        cal.set(lpd.getYear(), lpd.getMonth() - 1, lpd.getDay(), 0, 0, 0);

        SharedPreferences sp = getSharedPreferences(MyConstants.SHARED_PREFS_FILE, MODE_PRIVATE);
        sp.edit().putLong(MyConstants.KEY_LMP_DATE, cal.getTimeInMillis()).apply();
    }

    public void SaveUserInDBS() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();
        Calendar today = Calendar.getInstance();

        // 2. חילוץ רכיבי התאריך (ברירת מחדל: היום)
        int currentDay = today.get(Calendar.DAY_OF_MONTH);
        int currentMonth = today.get(Calendar.MONTH) + 1;
        int currentYear = today.get(Calendar.YEAR);

        // 3. יצירת אובייקטי התאריך
        LastPeriodDate lastPeriodDate = new LastPeriodDate(currentDay, currentMonth, currentYear);

        Calendar estimatedCal = (Calendar) today.clone();
        estimatedCal.add(Calendar.DAY_OF_YEAR, 280);

        EstimatedDate estimatedDate = new EstimatedDate(
                estimatedCal.get(Calendar.DAY_OF_MONTH),
                estimatedCal.get(Calendar.MONTH) + 1,
                estimatedCal.get(Calendar.YEAR)
        );

        User user = new User(etFullName.getText().toString(), etEmailRegister.getText().toString(), uid, lastPeriodDate, selectedRole, estimatedDate, etMobile.getText().toString());

        // עדכון ה-Singleton
        UserManager.setInstance(user);

        // ✅ עדכון ה-SharedPreferences (הגשר ל-JobService!)
        saveLMPToPrefs(lastPeriodDate);

        Log.d("MARIELA", "Save user:" + user.toString());

        // שמירת הציון במסד הנתונים
        userRef.child(uid).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MARIELA", "User saved successfully");
                    // כאן אפשר לעבור מסך, אבל אנחנו עושים את זה למטה ב-OnClickListener
                })
                .addOnFailureListener(e -> {
                    Log.e("MARIELA", "Failed to save user", e);
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        init();

        tvBackToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        fabMom.setOnClickListener(view -> {
            fabMom.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, R.color.teal_200));
            fabDad.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, android.R.color.darker_gray));
            selectedRole = "Mom";
        });

        fabDad.setOnClickListener(view -> {
            fabDad.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, R.color.teal_200));
            fabMom.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, android.R.color.darker_gray));
            selectedRole = "Dad";
        });

        btnRegister.setOnClickListener(view -> {
            if (verifyUser()) {
                String email = etEmailRegister.getText().toString();
                String password = etRegisterPassword.getText().toString();
                Log.d("MARIELA", "Register " + email);

                // ✅ שימוש בפונקציה התקנית של Firebase Auth
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();

                                // שמירת נתונים
                                SaveUserInDBS(); // שומר ל-Firebase + Singleton + SharedPrefs (LMP)
                                saveEmailToPrefs(email); // שומר אימייל ל-SharedPrefs

                                // מעבר למסך הבא
                                Intent intent = new Intent(RegisterActivity.this, TrackActivity.class);
                                startActivity(intent);
                                finish(); // סגירת ההרשמה
                            } else {
                                String err = task.getException() != null ? task.getException().getMessage() : "Error";
                                Toast.makeText(RegisterActivity.this, "Signup Failed: " + err, Toast.LENGTH_SHORT).show();
                                etEmailRegister.setError(err);
                            }
                        });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}