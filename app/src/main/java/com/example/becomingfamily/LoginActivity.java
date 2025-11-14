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
    private TextView tvForgotPassword,tvGoRegister;
    private ExtendedFloatingActionButton fabLogin;
    private  CurrentData currentData;
    private FirebaseDatabase database;
    private DatabaseReference userRef; // A reference to the root or a specific path
    public User user;
    public void init()
    {
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        tvForgotPassword=findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setPaintFlags(tvForgotPassword.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        tvGoRegister=findViewById(R.id.tvGoRegister);
        tvGoRegister.setPaintFlags(tvGoRegister.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        fabLogin=findViewById(R.id.fabLogin);
        currentData=new CurrentData();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        user=new User();

    }
    public boolean verifyData()
    {
        Log.d("MARIELA","verifyData");
        if (etEmail.getText().toString().trim().isEmpty()) {            etEmail.setError("הזן דואל");
            return false;
        }
        if (etPassword.getText().toString().trim().isEmpty())
        {
            etPassword.setError("הזן סיסמה");
            return false;
        }

        return true;
    }
    public CurrentData GetCurrentData(String email)
    {
        currentData.SetEmail(email);
        //verify dbs correct weeks-get last period and count weeks
        currentData.SetWeeks(1);

        return  currentData;
    }
    public void LoadUserFromDBS()
    {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("MARIELA","onDataChange "+dataSnapshot.getKey());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("MARIELA", "Child snapshot key: " + snapshot.getKey());
                    user = snapshot.getValue(User.class);
                    if (user != null && user.getEmail().equals(etEmail.getText().toString())) {
                        Log.d("MARIELA", "Retrieved user: " + user.toString());

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // טיפול בשגיאה
                Log.e("FirebaseRead", "Failed to read value.", databaseError.toException());

            }
        });
    }


    public void LoadCurrentData()
    {
        SharedPreferences sp=getSharedPreferences("BabySteps",MODE_PRIVATE);

        String email= sp.getString("email", "");
        etEmail.setText(email);

    }
    public void SaveCurrentData(CurrentData currentData)
    {
        SharedPreferences sp=getSharedPreferences("BabySteps",MODE_PRIVATE);
        SharedPreferences.Editor editor= sp.edit();
        editor.putString("email",currentData.GetEmail());
        editor.putInt("weeks",currentData.GetWeeks());
        editor.commit();
    }
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
                            currentData = GetCurrentData(email);
                            SaveCurrentData(currentData);

                            LoadUserFromDBS();
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, TrackActivity.class);
                            startActivity(intent);
                        } else {
                            etEmail.setError(task.getException().getMessage());

                            Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(LoginActivity.this, "Login Failed. " , Toast.LENGTH_SHORT).show();
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