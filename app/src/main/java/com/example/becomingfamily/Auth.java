package com.example.becomingfamily;
import android.app.Activity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Auth {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void signIn(Activity activity, String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, onCompleteListener);
    }

    public static void signOut() {
        auth.signOut();
    }

    public static void signUp(Activity activity, String email, String password, OnCompleteListener<AuthResult> onCompleteListener) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, onCompleteListener);
    }

    public static FirebaseUser getCurrentUser() {
        if (auth.getCurrentUser() == null)
            Log.d("Eitan Debug General", "Returning a null user");
        return auth.getCurrentUser();
    }
    public static void swapPassword(String newPassword, OnCompleteListener<Void> onCompleteListener) {
        FirebaseUser user = getCurrentUser();

        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(onCompleteListener);
        } else {
            // אם אין משתמש מחובר, מטפלים בכך כאן או ב-Activity הקורא.
            Log.e("MARIELA", "Cannot update password: No user is currently signed in.");
            // ניתן ליצור Task שנכשל כדי להחזיר שגיאה בצורה אחידה
            Task<Void> failedTask = com.google.android.gms.tasks.Tasks.forException(
                    new IllegalStateException("אין משתמש מחובר. לא ניתן לעדכן סיסמה."));
            failedTask.addOnCompleteListener(onCompleteListener);
        }
    }
}