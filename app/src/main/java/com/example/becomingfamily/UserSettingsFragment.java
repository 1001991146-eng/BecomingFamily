package com.example.becomingfamily;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class UserSettingsFragment extends Fragment {

    private ExtendedFloatingActionButton fabEMom, fabEDad;
    private TextInputEditText etEFullName,etEEmailRegister,etEMobile,etERegisterPassword,etEConfirmPassword;
    private Button btnEdit,btnELogout,btnEDelete;
    private User user;
    private  int week;
    private Context context;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    private FirebaseAuth mAuth; // חשוב!
    private FirebaseUser firebaseUser; // חשוב!

    public UserSettingsFragment(Context context, int week) {
        // Required empty public constructor
        this.week=week;
        this.context=context;
        user=new User();

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();


    }
    // --- לוגיקת מחיקת משתמש (Auth + DB) ---
    private void deleteUserAccount(final String password) {
        if (firebaseUser == null || user.getEmail() == null) {
            Toast.makeText(context, "שגיאה: משתמש לא מחובר או חסר אימייל.", Toast.LENGTH_LONG).show();
            return;
        }

        final String email = user.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        // שלב 1: אימות מחדש (Re-authenticate)
        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> reauthTask) {
                        if (reauthTask.isSuccessful()) {
                            Log.d("MARIELA", "Re-authentication successful.");
                            // שלב 2: מחיקת חשבון Authentication
                            firebaseUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> deleteTask) {
                                            if (deleteTask.isSuccessful()) {
                                                Log.d("MARIELA", "Firebase Auth account deleted successfully.");
                                                // שלב 3: מחיקת נתונים מ-Realtime Database
                                                deleteUserDataFromRealtimeDB(email);
                                            } else {
                                                Log.e("MARIELA", "Firebase Auth delete failed.", deleteTask.getException());
                                                Toast.makeText(context, "שגיאה במחיקת חשבון האימות. נסה שוב.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Log.e("MARIELA", "Re-authentication failed.", reauthTask.getException());
                            Toast.makeText(context, "הסיסמה שגויה או נדרש התחברות נוספת.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    // --- לוגיקת מחיקת נתונים מ-Realtime DB ---
    private void deleteUserDataFromRealtimeDB(String emailToDelete) {
        userRef.orderByChild("email").equalTo(emailToDelete)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("MARIELA", "User data for " + emailToDelete + " deleted successfully from Realtime DB.");
                                            Toast.makeText(context, "החשבון נמחק בהצלחה.", Toast.LENGTH_LONG).show();
                                            // לאחר מחיקה מלאה, נווט למסך ההתחברות/ראשי
                                            Intent intent = new Intent(context, MainActivity.class);
                                            // נקה את כל ה-Activities הקודמים מה-stack
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("MARIELA", "Failed to delete user data for " + emailToDelete, e);
                                            Toast.makeText(context, "שגיאה במחיקת הנתונים. נסה שוב.", Toast.LENGTH_LONG).show();
                                        });
                            }
                        } else {
                            Log.d("MARIELA", "User data for " + emailToDelete + " not found in Realtime DB. Continuing...");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("MARIELA", "Realtime Database query cancelled: " + databaseError.getMessage());
                        Toast.makeText(context, "שגיאה בגישה לבסיס הנתונים.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --- לוגיקת הצגת דיאלוג אימות מחדש (Re-authentication) ---
    private void showReauthDialog() {
        if (firebaseUser == null) {
            Toast.makeText(context, "שגיאה: המשתמש אינו מחובר.", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת EditText לסיסמה בתוך הדיאלוג
        final EditText passwordInput = new EditText(context);
        passwordInput.setHint("הכנס סיסמה נוכחית");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("אימות מחדש");
        builder.setMessage("אנא הזן את סיסמתך לאישור סופי של מחיקת החשבון:");
        builder.setView(passwordInput); // הוספת שדה הסיסמה לדיאלוג

        builder.setPositiveButton("אשר ומחק", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String password = passwordInput.getText().toString();
                if (password.isEmpty()) {
                    Toast.makeText(context, "הסיסמה אינה יכולה להיות ריקה.", Toast.LENGTH_SHORT).show();
                } else {
                    deleteUserAccount(password);
                    // מחיקת משתמש
                    Intent intent=new Intent(context,MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        builder.setNegativeButton("בטל", null);
        builder.create().show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_user_settings, container, false);
        fabEMom=v.findViewById(R.id.fabEMom);
        fabEDad=v.findViewById(R.id.fabEDad);
        etEFullName=v.findViewById(R.id.etEFullName);
        etEEmailRegister=v.findViewById(R.id.etEEmailRegister);
        etEMobile=v.findViewById(R.id.etEMobile);
        etERegisterPassword=v.findViewById(R.id.etERegisterPassword);
        etEConfirmPassword=v.findViewById(R.id.etEConfirmPassword);
        btnEdit=v.findViewById(R.id.btnEdit);
        btnELogout=v.findViewById(R.id.btnELogout);
        btnEDelete=v.findViewById(R.id.btnEDelete);

        if (user.getRole().equals("Mom"))
        {
            fabEMom.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.teal_200));
            fabEDad.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
        }
        else {
            fabEDad.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.teal_200));
            fabEMom.setBackgroundTintList(ContextCompat.getColorStateList(context, android.R.color.darker_gray));
        }

        etEFullName.setText(user.getFullName());
        etEEmailRegister.setText(user.getEmail());
        etEMobile.setText(user.getPhone());
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btnEDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adbCorrectResponse=new AlertDialog.Builder(context);
                adbCorrectResponse.setTitle("מחיקת משתמש");
                adbCorrectResponse.setMessage("האם את בטוחה שברצונך למחוק משתמש זה?");
                adbCorrectResponse.setCancelable(true);
                adbCorrectResponse.setIcon(R.drawable.baby);
                adbCorrectResponse.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
// IF YES

                        showReauthDialog();

                    }
                });
                adbCorrectResponse.setNegativeButton("לא", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle cancel button click if needed
                        // This is where you can add actions for the cancel button
                        //IF NO

                    }
                });
                adbCorrectResponse.create().show();
            }
        });
        btnELogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adbCorrectResponse=new AlertDialog.Builder(context);
                adbCorrectResponse.setTitle("התנתקות");
                adbCorrectResponse.setMessage("האם את בטוחה שברצונך להתנתק?");
                adbCorrectResponse.setCancelable(true);
                adbCorrectResponse.setIcon(R.drawable.baby);
                adbCorrectResponse.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
// IF YES

                        Auth.signOut();
                        Intent intent=new Intent(context,MainActivity.class);
                        startActivity(intent);
                    }
                });
                adbCorrectResponse.setNegativeButton("לא", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle cancel button click if needed
                        // This is where you can add actions for the cancel button
                        //IF NO

                    }
                });
                adbCorrectResponse.create().show();


            }
        });
        return v;
    }

}