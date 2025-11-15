package com.example.becomingfamily;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    private ExtendedFloatingActionButton fabDad,fabMom;
    private TextInputEditText etFullName,etEmailRegister,etRegisterPassword,etConfirmPassword, etMobile;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private CurrentData currentData;
    private String selectedRole="Mom";
    private FirebaseDatabase database;
    private DatabaseReference userRef; // A reference to the root or a specific path

    public void init()
    {
        fabDad=findViewById(R.id.fabDad);
        fabMom=findViewById(R.id.fabMom);
        etFullName=findViewById(R.id.etFullName);
        etRegisterPassword=findViewById(R.id.etRegisterPassword);
        etConfirmPassword=findViewById(R.id.etConfirmPassword);
        etEmailRegister=findViewById(R.id.etEmailRegister);
        btnRegister=findViewById(R.id.btnRegister);
        etMobile=findViewById(R.id.etMobile);

        tvBackToLogin=findViewById(R.id.tvBackToLogin);
        tvBackToLogin.setPaintFlags(tvBackToLogin.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        currentData=new CurrentData();
        fabMom.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.teal_200));
        fabDad.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        selectedRole = "Mom";
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");

    }
    public boolean verifyUser()
    {
        if (etEmailRegister.getText().toString().trim().isEmpty())
        {
            etEmailRegister.setError("Missing email");
            return false;

        }
        if (etMobile.getText().toString().trim().isEmpty())
        {
            etMobile.setError("Missing phone");
            return false;
        }
        if (etRegisterPassword.getText().toString().trim().isEmpty())
        {
            etRegisterPassword.setError("Missing password");
            return false;
        }
        if (etRegisterPassword.getText().toString().length()<6) {
            etRegisterPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!etRegisterPassword.getText().toString().equals(etConfirmPassword.getText().toString()))
        {
            etConfirmPassword.setError("Password must be the same");
            return false;
        }
        return  true;
    }
    public void SaveUserInDBS()
    {
        String uid=Auth.getCurrentUser().getUid();
        Calendar today = Calendar.getInstance();

// 2. חילוץ רכיבי התאריך
        int currentDay = today.get(Calendar.DAY_OF_MONTH);
// חודש: Calendar מחזיר 0-11, לכן אנו מוסיפים 1 כדי לשמור ב-Firebase 1-12
        int currentMonth = today.get(Calendar.MONTH) + 1;
        int currentYear = today.get(Calendar.YEAR);

        // 3. יצירת אובייקטי התאריך
// תאריך המחזור האחרון יוגדר להיום
        LastPeriodDate lastPeriodDate = new LastPeriodDate(currentDay, currentMonth, currentYear);
// תאריך הלידה המשוער יחושב מהתאריך הנוכחי (+280 יום)
// אנו משכפלים את ה-Calendar הנוכחי ומוסיפים 280 יום
        Calendar estimatedCal = (Calendar) today.clone();
        estimatedCal.add(Calendar.DAY_OF_YEAR, 280);

        EstimatedDate estimatedDate = new EstimatedDate(
                estimatedCal.get(Calendar.DAY_OF_MONTH),
                estimatedCal.get(Calendar.MONTH) + 1, // תיקון החודש לטווח 1-12
                estimatedCal.get(Calendar.YEAR)
        );


        User user=new User(etFullName.getText().toString(),etEmailRegister.getText().toString(),uid,lastPeriodDate,selectedRole,estimatedDate,etMobile.getText().toString());
        Log.d("MARIELA","Save user:"+user.toString());
        // שמירת הציון במסד הנתונים
        userRef.child(uid).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    // הצלחת השמירה
                    Log.d("MARIELA", "User properties added successfully for " + user.getEmail());
                })
                .addOnFailureListener(e -> {
                    // כישלון בשמירה
                    Log.e("MARIELA", "Failed to add user properties for " + user.getEmail(), e);
                });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        init();
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        fabMom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMom.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, R.color.teal_200));
                fabDad.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, android.R.color.darker_gray));
                selectedRole = "Mom";
            }
        });

        fabDad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabDad.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, R.color.teal_200));
                fabMom.setBackgroundTintList(ContextCompat.getColorStateList(RegisterActivity.this, android.R.color.darker_gray));
                selectedRole = "Dad";
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyUser())
                {
                    String email=etEmailRegister.getText().toString();
                    String password=etRegisterPassword.getText().toString();
                    Log.d("MARIELA","Register "+email);

                    Auth.signUp(RegisterActivity.this, email, password, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                            currentData.SetRole(selectedRole);
                            currentData.SetEmail(email);
                            currentData.SetWeeks(1);

                            SaveUserInDBS();
                            Intent intent=new Intent(RegisterActivity.this,TrackActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Signup Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                            etEmailRegister.setError(task.getException().getMessage());
                        }
                    });
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}