package com.example.becomingfamily;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
/*
* TBD -   
* get me pic of baby in this step
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
        SharedPreferences sp=getSharedPreferences(MyConstants.SHARED_PREFS_FILE,MODE_PRIVATE);
        int weeks= sp.getInt(MyConstants.KEY_WEEKS,1);
        Log.d("MARIELA","Show week "+Integer.toString(weeks));
        progressBar.setProgress(weeks);
    }

    private void askNotificationPermission() {
        // בודק אם גרסת האנדרואיד היא 13 (Tiramisu) ומעלה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // אם אין הרשאה - מבקש אותה מהמשתמש
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        init();
        getPrevDataSaved();
// 2. בקשת הרשאה להתראות (קריטי ל-JobService!)
        askNotificationPermission();
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