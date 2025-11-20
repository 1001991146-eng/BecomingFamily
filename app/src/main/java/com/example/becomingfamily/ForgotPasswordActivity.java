package com.example.becomingfamily;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText et_reset_email;
    private Button btn_reset_password;
    private  CurrentData currentData;
    public User user;
    private FirebaseDatabase database;
    private DatabaseReference userRef; // A reference to the root or a specific path
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    public void init()
    {

        et_reset_email=findViewById(R.id.et_reset_email);
        btn_reset_password=findViewById(R.id.btn_reset_password);
        // init loged in user data
        currentData=new CurrentData();// email
        user=new User(); // create empty user data*/
        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress_bar_reset);

    }
    private void resetPassword() {
        String email = et_reset_email.getText().toString().trim();

        // 1. בדיקות תקינות
        if (TextUtils.isEmpty(email)) {
            et_reset_email.setError("יש להזין כתובת דוא\"ל.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_reset_email.setError("כתובת הדוא\"ל אינה תקינה.");
            return;
        }

        // 2. הצגת סרגל התקדמות ונטרול כפתור
        progressBar.setVisibility(View.VISIBLE);
        btn_reset_password.setEnabled(false);

        // 3. קריאה לפונקציה של Firebase לאיפוס סיסמה
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btn_reset_password.setEnabled(true);

                    if (task.isSuccessful()) {
                        // הצלחה: הודעה למשתמש לבדוק את הדוא"ל שלו
                        Toast.makeText(ForgotPasswordActivity.this,
                                "נשלח קישור לאיפוס סיסמה לכתובת הדוא\"ל שלך. אנא בדקי את תיבת הדואר.",
                                Toast.LENGTH_LONG).show();

                        // אופציונלי: סגירת המסך וחזרה למסך ההתחברות לאחר שליחה מוצלחת
                        finish();
                    } else {
                        // כישלון: הצגת הודעת שגיאה
                        String errorMessage = "שגיאה באיפוס הסיסמה. נסי שוב.";

                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            // אם המשתמש לא קיים או הדוא"ל לא תקין
                            errorMessage = "הדוא\"ל שהוזן אינו רשום או אינו תקין.";
                        } else if (task.getException() != null) {
                            Log.e("ForgotPassword", "Firebase Error: " + task.getException().getMessage());
                            errorMessage = "שגיאה: ודאי שהדוא\"ל שהוזן נכון והחיבור פעיל.";
                        }
                        Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        init();
        btn_reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_password_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}