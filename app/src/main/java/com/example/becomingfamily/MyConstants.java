package com.example.becomingfamily;

public class MyConstants
{
    // 1. שם הקובץ של SharedPreferences (הקובץ הפיזי בטלפון)
    // במקום שכל Activity ימציא שם משלו, כולם משתמשים בזה:
    public static final String SHARED_PREFS_FILE = "BabySteps";

    // 2. המפתחות (Keys) לשמירת נתונים בתוך הקובץ
    // במקום לזכור אם כתבת "LMP" או "LastPeriod", יש לך קבוע:
    public static final String KEY_LMP_DATE = "LMP_DATE_MILLIS";
    public static final String KEY_WEEKS = "weeks";

    public static final String KEY_LAST_NOTIFIED_WEEK = "LastNotifiedWeek";

    // (אופציונלי) מפתח לשמירת האימייל ללוגין הבא
    public static final String KEY_EMAIL = "email";
}
