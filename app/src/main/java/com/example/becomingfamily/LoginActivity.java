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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail,etPassword;
    private TextView tvForgotPassword,tvGoRegister;
    private ExtendedFloatingActionButton fabLogin;
    private  CurrentData currentData;
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

                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, TrackActivity.class);
                            startActivity(intent);
                        } else {
                            etEmail.setError(task.getException().toString());
                            etPassword.setError(task.getException().toString());
                            Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(LoginActivity.this, "Login Failed. " , Toast.LENGTH_SHORT).show();
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