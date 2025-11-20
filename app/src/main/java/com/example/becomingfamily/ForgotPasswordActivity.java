package com.example.becomingfamily;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView tvFError;
    private TextInputEditText etFEmail,etFPhone,etCode;
    private ExtendedFloatingActionButton fabSendSMS,fabFLogin;
    private  CurrentData currentData;
    public User user;
    public static ProgressBar pbBussy;
    public static String code;
    private FirebaseDatabase database;
    private DatabaseReference userRef; // A reference to the root or a specific path
    /*
    public SMSReceiver smsReceiver;
    public IntentFilter intentFilterSMS;
    public HelpHandler handler;
    public CountDownTimer cdt;
    public final int MAX_TIME=6000;
    public final int SECOND=1000;
    public long missing ;
*/
    public void init()
    {
        etFEmail=findViewById(R.id.etFEmail);
        etFPhone=findViewById(R.id.etFPhone);
        etCode=findViewById(R.id.etCode);
        fabSendSMS=findViewById(R.id.fabSendSMS);
        fabFLogin=findViewById(R.id.fabFLogin);
        pbBussy=findViewById(R.id.pbBussy);
        tvFError=findViewById(R.id.tvFError);
        code="-1";
        // init loged in user data
        currentData=new CurrentData();// email
        user=new User(); // create empty user data
    }
    /*
    public  int getRandomCode()
    {
        int code=0;
        Random random = new Random();
        // צור מספר אקראי בין 100000 ל-999999
        code = random.nextInt(1000000) - 100000;
        Log.d("MARIELA","random:"+code);
        return  code;
    }
    public void ask4Permissions()
    {
        String[] permissions = {android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.RECEIVE_SMS
        };
        ActivityCompat.requestPermissions(ForgotPasswordActivity.this,
                permissions,
                1);
    }
    public void register4Broadcast()
    {
        smsReceiver = new SMSReceiver();
        intentFilterSMS = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilterSMS);
    }
    public void sendSMS()
    {
        String phoneNum=etFPhone.getText().toString();
        // verify phone num same as DBS
        code=Integer.toString(getRandomCode());
        String msg=code;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNum, null, msg, null, null);
            Toast.makeText(ForgotPasswordActivity.this, "Sent to:" +phoneNum+" msg: "+ msg,Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(ForgotPasswordActivity.this, "Failed to send SMS:" + e.getMessage(),Toast.LENGTH_LONG).show();
        }
        fabSendSMS.setEnabled(false);
        pbBussy.setVisibility(View.VISIBLE);
        if (cdt!=null)
        {
            cdt.cancel();
        }
        missing=MAX_TIME;
        cdt=new CountDownTimer(missing,SECOND) {
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                fabSendSMS.setEnabled(true);
                pbBussy.setVisibility(View.INVISIBLE);
                if (cdt != null) {
                    cdt.cancel();
                    cdt = null;
                }
            }
        }.start();

    }
    public void defineHandler()
    {
        handler=new HelpHandler(ForgotPasswordActivity.this);

    }
*/
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
        userRef.orderByChild("email").equalTo(etFEmail.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                // snapshot הוא ה-UID של המשתמש (למשל, Q6SS...)
                                User user = snapshot.getValue(User.class);

                                if (user != null) {
                                    Log.d("MARIELA", "Retrieved user: " + user.toString());
                                    tvFError.setText("User retrieved successfully.");
                                    // מצאנו את המשתמש, אפשר לצאת מהלולאה
                                    Intent intent = new Intent(ForgotPasswordActivity.this, WeeklyUpdateActivity.class);
                                    startActivity(intent);
                                    break;
                                }
                            }
                        } else {
                            // לא נמצא משתמש עם האימייל הזה
                            tvFError.setText("User not found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvFError.setText(error.toException().toString());

                    }

                    // ... onCancelled כפי שהיה ...
                });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        init();
        /*
        ask4Permissions();
       defineHandler();
        fabSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*sendSMS();
                if  (etCode.getText().toString().equals(ForgotPasswordActivity.code)) {

                    Auth.signIn(ForgotPasswordActivity.this, user.getEmail(), password, task -> {
                        if (task.isSuccessful()) {
                            tvFError.setText("Login succeded.");
                            currentData = SavePreviousUser(email);
                            LoadUserFromDBS();
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();



                        } else {
                            tvFError.setText(task.getException().getMessage());
                            Toast.makeText(ForgotPasswordActivity.this, "Login Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(ForgotPasswordActivity.this, "Login Failed. " , Toast.LENGTH_SHORT).show();
                    tvFError.setText("Login Failed.");

                }
                    Intent it = new Intent(ForgotPasswordActivity.this, WeeklyUpdateActivity.class);
                    startActivity(it);





                }
        });
*/
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}