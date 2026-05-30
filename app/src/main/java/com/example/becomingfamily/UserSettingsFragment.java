package com.example.becomingfamily;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
    private TextInputEditText etEFullName,etEEmailRegister,etEMobile,etERegisterPassword,etEConfirmPassword,etEOriginalRegisterPassword;
    private Button btnEdit,btnELogout,btnEDelete,btnTrack;
    private TextView tvSavesDates;
    private User user;
    private int week;
    private int days;
    private String  newRole;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    private FirebaseAuth mAuth; // חשוב!
    private FirebaseUser firebaseUser; // חשוב!
    public static final String BABY_FRAGMENT_TAG = "my_baby_fragment"; // תג קבוע

    public UserSettingsFragment() {
        // Required empty public constructor
    }

    public static UserSettingsFragment newInstance(int week, int days) {
        UserSettingsFragment fragment = new UserSettingsFragment();
        Bundle args = new Bundle();
        args.putInt("week", week);
        args.putInt("days", days);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            week = getArguments().getInt("week");
            days = getArguments().getInt("days");
        }
        user = UserManager.getInstance();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
    }
    // --- לוגיקת מחיקת משתמש (Auth + DB) ---
    private void deleteUserAccount(final String password) {
        if (firebaseUser == null || user.getEmail() == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "שגיאה: משתמש לא מחובר או חסר אימייל.", LENGTH_LONG).show();
            }
            return;
        }

        final String email = UserManager.getInstance().getEmail();
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
                                                deleteUserDataFromRealtimeDB(firebaseUser.getUid());
                                            } else {
                                                Log.e("MARIELA", "Firebase Auth delete failed.", deleteTask.getException());
                                                if (isAdded()) {
                                                    Toast.makeText(requireContext(), "שגיאה במחיקת חשבון האימות. נסה שוב.", LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            Log.e("MARIELA", "Re-authentication failed.", reauthTask.getException());
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "הסיסמה שגויה או נדרש התחברות נוספת.", LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
    // --- לוגיקת מחיקת נתונים מ-Realtime DB ---
    private void deleteUserDataFromRealtimeDB(String uid) {
        userRef.child(uid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("MARIELA", "User data for " + uid + " deleted successfully from Realtime DB.");
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "החשבון נמחק בהצלחה.", LENGTH_LONG).show();
                        // לאחר מחיקה מלאה, נווט למסך ההתחברות/ראשי
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        // נקה את כל ה-Activities הקודמים מה-stack
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MARIELA", "Failed to delete user data for " + uid, e);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "שגיאה במחיקת הנתונים. נסה שוב.", LENGTH_LONG).show();
                    }
                });
    }

    // --- לוגיקת הצגת דיאלוג אימות מחדש (Re-authentication) ---
    private void showReauthDialog() {
        if (firebaseUser == null) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "שגיאה: המשתמש אינו מחובר.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // יצירת EditText לסיסמה בתוך הדיאלוג
        final EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint("הכנס סיסמה נוכחית");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("אימות מחדש");
        builder.setMessage("אנא הזן את סיסמתך לאישור סופי של מחיקת החשבון:");
        builder.setView(passwordInput); // הוספת שדה הסיסמה לדיאלוג

        builder.setPositiveButton("אשר ומחק", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String password = passwordInput.getText().toString();
                if (password.isEmpty()) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "הסיסמה אינה יכולה להיות ריקה.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    deleteUserAccount(password);
                }
            }
        });

        builder.setNegativeButton("בטל", null);
        builder.create().show();
    }
    public boolean verifyUser()
    {
        if (etEOriginalRegisterPassword.getText().toString().trim().isEmpty())
        {
            etEOriginalRegisterPassword.setError("Missing email");
        }
        if (etEFullName.getText().toString().trim().isEmpty())
        {
            etEFullName.setError("Missing email");
            return false;

        }
        if (etEMobile.getText().toString().trim().isEmpty())
        {
            etEMobile.setError("Missing phone");
            return false;
        }
        if (etERegisterPassword.getText().toString().trim().isEmpty())
        {
            etERegisterPassword.setError("Missing password");
            return false;
        }
        if (etERegisterPassword.getText().toString().length()<6) {
            etERegisterPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!etERegisterPassword.getText().toString().equals(etEConfirmPassword.getText().toString()))
        {
            etERegisterPassword.setError("Password must be the same");
            return false;
        }
        return  true;
    }
    public void resetToMyBabyFragment() {
        FragmentManager fm = getParentFragmentManager();
        // 1. ניקוי מוחלט של ערימת החזרה
        // null ו-POP_BACK_STACK_INCLUSIVE מוחק את הכל.
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // 2. יצירה והחלפה ל-MyBabyFragment כפרגמנט הבסיס החדש
        // אנחנו לא מוסיפים את זה ל-Back Stack כדי שכפתור ה-Back יצא מה-Activity
        MyBabyFragment newBabyFragment = new MyBabyFragment(requireActivity(), week,days);
        fm.beginTransaction()
                .replace(R.id.fragment_container, newBabyFragment, BABY_FRAGMENT_TAG)
                .commit();

        // 3. ודא שהטרנזקציות הקודמות הסתיימו
        fm.executePendingTransactions();
    }

    public void SaveDataInFirebase()
    {
        if (verifyUser())
        {
            String uid=mAuth.getCurrentUser().getUid();
            user=UserManager.getInstance();
            user.setFullName(etEFullName.getText().toString());
            user.setEmail(etEEmailRegister.getText().toString());
            user.setPhone(etEMobile.getText().toString());
            user.setRole(newRole);
            UserManager.setInstance(user);
            // verify old passord
            // 1. יצירת Credential מהסיסמה הישנה
            AuthCredential credential = EmailAuthProvider.getCredential(
                        mAuth.getCurrentUser().getEmail(),
                        etEOriginalRegisterPassword.getText().toString()
            );
            mAuth.getCurrentUser().reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 3. אימות מחדש הצליח, כעת ניתן לשנות את הסיסמה
                            mAuth.getCurrentUser().updatePassword(etERegisterPassword.getText().toString())
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            if (isAdded()) {
                                                Toast.makeText(requireContext(), "הסיסמה עודכנה בהצלחה!", Toast.LENGTH_SHORT).show();
                                            }

                                            Log.d("MARIELA","Save user:"+user.toString());
                                            userRef.child(uid).setValue(user)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("MARIELA", "User properties for " + user.getEmail() + " updated successfully.");
                                                        if (isAdded()) {
                                                            Toast.makeText(requireContext(),"שינויים נשמרו", LENGTH_LONG).show();
                                                        }
                                                        resetToMyBabyFragment();
                                                        Log.d("MARIELA","goto baby fragment");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("MARIELA", "Failed to update user properties for " + user.getEmail(), e);
                                                    });
                                        } else {
                                            Log.e("ChangePassword", "עדכון סיסמה נכשל", updateTask.getException());
                                            if (isAdded()) {
                                                Toast.makeText(requireContext(), "עדכון סיסמה נכשל.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // אימות מחדש נכשל (הסיסמה הישנה שגויה)
                            Log.e("ChangePassword", "אימות מחדש נכשל", task.getException());
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "הסיסמה הנוכחית שגויה.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
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
        btnTrack=v.findViewById(R.id.btnTrack);
        etEOriginalRegisterPassword=v.findViewById(R.id.etEOriginalRegisterPassword);
        tvSavesDates=v.findViewById(R.id.tvSavesDates);

        if (user.getRole().equals("Mom"))
        {
            newRole="Mom";
            fabEMom.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.teal_200));
            fabEDad.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
        }
        else {
            newRole="Dad";
            fabEDad.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.teal_200));
            fabEMom.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
        }

        etEFullName.setText(user.getFullName());
        etEEmailRegister.setText(user.getEmail());
        etEMobile.setText(user.getPhone());
        String lastPeriod=user.getLastPeriodDate().getDay()+"/"+user.getLastPeriodDate().getMonth()+"/"+user.getLastPeriodDate().getYear();
        String estimatedDate=user.getEstimatedDate().getDay()+"/"+user.getEstimatedDate().getMonth()+"/"+user.getEstimatedDate().getYear();
        tvSavesDates.setText("תאריך מחזור אחרון: "+lastPeriod+"\n תאריך לידה משוער: "+estimatedDate);
        fabEDad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newRole="Dad";
                fabEDad.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.teal_200));
                fabEMom.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));
            }
        });
        fabEMom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newRole="Mom";
                fabEMom.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.teal_200));
                fabEDad.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray));

            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveDataInFirebase();

            }
        });
        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(requireContext(),TrackActivity.class);
                startActivity(intent);
            }
        });
        btnEDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adbCorrectResponse=new AlertDialog.Builder(requireContext());
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
                AlertDialog.Builder adbCorrectResponse=new AlertDialog.Builder(requireContext());
                adbCorrectResponse.setTitle("התנתקות");
                adbCorrectResponse.setMessage("האם את בטוחה שברצונך להתנתק?");
                adbCorrectResponse.setCancelable(true);
                adbCorrectResponse.setIcon(R.drawable.baby);
                adbCorrectResponse.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
// IF YES

                        Auth.signOut();
                        // בפונקציה של כפתור ההתנתקות:
                        UserManager.clear();
                        Intent intent=new Intent(requireContext(),MainActivity.class);
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