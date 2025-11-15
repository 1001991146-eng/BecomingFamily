package com.example.becomingfamily;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail,etPassword;
    private TextView tvForgotPassword,tvGoRegister,tvError;
    private ExtendedFloatingActionButton fabLogin;
    private  CurrentData currentData;
    private FirebaseDatabase database;
    private DatabaseReference userRef; // A reference to the root or a specific path
    public User user;

    /* init elements on activity*/
    public void init()
    {
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        tvForgotPassword=findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setPaintFlags(tvForgotPassword.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        tvError=findViewById(R.id.tvError);
        tvGoRegister=findViewById(R.id.tvGoRegister);
        // display as link
        tvGoRegister.setPaintFlags(tvGoRegister.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        fabLogin=findViewById(R.id.fabLogin);
        // firebase init
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");

        // init loged in user data
        currentData=new CurrentData();// email
        user=new User(); // create empty user data

        tvError.setText("");
    }
    /* verify all data filled in login activity */
    public boolean verifyData()
    {
        Log.d("MARIELA","verifyData");
        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("הזן דואל");
            return false;
        }
        if (etPassword.getText().toString().trim().isEmpty())
        {
            etPassword.setError("הזן סיסמה");
            return false;
        }
        return true;
    }
    /* save current user as previous loged in user */
    public CurrentData SavePreviousUser(String email)
    {
        currentData.SetEmail(email);
        SharedPreferences sp=getSharedPreferences("BabySteps",MODE_PRIVATE);
        SharedPreferences.Editor editor= sp.edit();
        editor.putString("email",currentData.GetEmail());
        editor.commit();
        return  currentData;
    }
    /* get user data from firebase based on email */
    public void LoadUserFromDBS()
    {
        // יוצר שאילתה שמחפשת משתמשים שבהם השדה "email" שווה לערך המבוקש.
        userRef.orderByChild("email").equalTo(etEmail.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // snapshot הוא ה-UID של המשתמש (למשל, Q6SS...)
                                User user = snapshot.getValue(User.class);

                                if (user != null) {
                                    Log.d("MARIELA", "Retrieved user: " + user.toString());
                                    tvError.setText("User retrieved successfully.");
                                    // מצאנו את המשתמש, אפשר לצאת מהלולאה
                                    Intent intent = new Intent(LoginActivity.this, WeeklyUpdateActivity.class);
                                    startActivity(intent);
                                    break;
                                }
                            }
                        } else {
                            // לא נמצא משתמש עם האימייל הזה
                            tvError.setText("User not found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvError.setText(error.toException().toString());

                    }

                    // ... onCancelled כפי שהיה ...
                });
    }


/* load previous loged in user from shared prefferences*/
    public void LoadCurrentData()
    {
        SharedPreferences sp=getSharedPreferences("BabySteps",MODE_PRIVATE);
        String email= sp.getString("email", "");
        etEmail.setText(email);
    }

    /* main */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        init();
        LoadCurrentData();

        fabLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=etEmail.getText().toString();
                String password=etPassword.getText().toString();
                Log.d("MARIELA","Login "+email);
                if (verifyData()) {
                    Auth.signIn(LoginActivity.this, email, password, task -> {
                        if (task.isSuccessful()) {
                            tvError.setText("Login succeded.");
                            currentData = SavePreviousUser(email);
                            LoadUserFromDBS();
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();



                        } else {
                            tvError.setText(task.getException().getMessage());
                            Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(LoginActivity.this, "Login Failed. " , Toast.LENGTH_SHORT).show();
                    tvError.setText("Login Failed.");

                }
            }
        });
        tvGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}