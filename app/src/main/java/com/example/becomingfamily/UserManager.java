package com.example.becomingfamily;

public class UserManager {
    // זה המשתנה הסטטי היחיד שתצטרכי!
    private static User currentUser;

    // קבלת המשתמש (במקום User.get...)
    public static User getInstance() {
        return currentUser;
    }

    // הגדרת משתמש (במקום User.set...) - קורה בלוגין
    public static void setInstance(User user) {
        currentUser = user;
    }

    // יציאה מהמערכת
    public static void clear() {
        currentUser = null;
    }
}
