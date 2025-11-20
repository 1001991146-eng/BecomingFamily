package com.example.becomingfamily;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
/*
* TBD -   broadcast reciever for sms - forgot password
*  broadcast reciever for network disconnction - firebase stops working!
* edit user settings: name, phone, password, lastperiod, role
* * */
public class MainActivity extends AppCompatActivity {
    private ExtendedFloatingActionButton fabStart;
    private ProgressBar progressBar;
    /*init elements on activity*/
    public void init()
    {
        fabStart=findViewById(R.id.fabStart);
        progressBar=findViewById(R.id.progressBarHorizontal);

    }
    /* show progress on pregnancy by shared prefferences */
    public void getPrevDataSaved()
    {
        SharedPreferences sp=getSharedPreferences("BabySteps",MODE_PRIVATE);
        int weeks= sp.getInt("weeks",1);
        Log.d("MARIELA","Show week "+Integer.toString(weeks));
        progressBar.setProgress(weeks);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        init();
        getPrevDataSaved();

        fabStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
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