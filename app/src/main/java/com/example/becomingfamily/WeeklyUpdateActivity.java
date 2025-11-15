package com.example.becomingfamily;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

public class WeeklyUpdateActivity extends AppCompatActivity {
    private MyBabyFragment babyFragment;
    private UserSettingsFragment userSettingsFragment;
    private TestsFragment testsFragment;
    private YouFragment youFragment;
    private Button btn_growth, btn_my_life,btn_tests,btn_user_settings;

    public void init()
    {
        btn_growth=findViewById(R.id.btn_growth);
        btn_my_life=findViewById(R.id.btn_my_life);
        btn_tests=findViewById(R.id.btn_tests);
        btn_user_settings=findViewById(R.id.btn_user_settings);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weekly_update);
        init();
        babyFragment=new MyBabyFragment();
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, babyFragment);
        ft.commit();

        Log.d("MARIELA","WeeklyUpdateActivity");
        btn_growth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                babyFragment=new MyBabyFragment();
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, babyFragment);
                ft.commit();
            }
        });
        btn_my_life.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youFragment=new YouFragment();
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, youFragment);
                ft.commit();
            }
        });
        btn_tests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testsFragment=new TestsFragment();
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, testsFragment);
                ft.commit();
            }
        });
        btn_user_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSettingsFragment=new UserSettingsFragment();
                FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, userSettingsFragment);
                ft.commit();
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.weelkyUpdate), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}