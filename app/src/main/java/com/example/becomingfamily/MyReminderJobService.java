package com.example.becomingfamily;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class MyReminderJobService extends JobService {
private int currentWeek;
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
// המקום להתחיל את העבודה שלך!

        new Thread(() -> {
            // 2. ה-Thread קורא לפונקציה הלוגית שלנו:
            if (isNewPregnancyWeek()) {

                // ... אם True, שולחים PUSH...
                sendPushNotification("ברכות! נכנסת לשבוע הריון " + currentWeek);
                saveLastNotifiedWeek(currentWeek); // חשוב!
            }
            // חשוב! קוראים ל-jobFinished כשהעבודה הסתיימה
            jobFinished(jobParameters, false);
        }).start();


        return true; // אומר למערכת שיש עבודה אסינכרונית (אם צריך)
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        // מופעל אם המערכת צריכה להפסיק את העבודה באמצע (למשל, חסכון בסוללה).
        return true;
    }

    private long getLMPDateMillisFromStorage() {
        // משתמשים באותו שם קובץ, אבל במפתח אחר!
        SharedPreferences prefs = getSharedPreferences(MyConstants.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        // משתמשים במפתח "LMP_DATE_MILLIS"
        return prefs.getLong(MyConstants.KEY_LMP_DATE, 0);
    }
    private boolean isNewPregnancyWeek() {
        long lmpMillis = getLMPDateMillisFromStorage();
        if (lmpMillis == 0) return false;

        WeekCalculator calculator = new WeekCalculator(lmpMillis);
        currentWeek = calculator.getWeek();

        // בדיקה אם עבר שבוע מאז ההתראה האחרונה
        int lastNotifiedWeek = getLastNotifiedWeekFromStorage();
        return (currentWeek > lastNotifiedWeek && currentWeek <= 42);
    }

    private int getLastNotifiedWeekFromStorage() {
        SharedPreferences prefs = getSharedPreferences(MyConstants.SHARED_PREFS_FILE,Context.MODE_PRIVATE);
        // אם המשתנה לא קיים (הפעם הראשונה), ברירת המחדל תהיה 0.
        return prefs.getInt(MyConstants.KEY_LAST_NOTIFIED_WEEK, 0);
    }
    private void saveLastNotifiedWeek(int weekNumber) {
        // שלב 1: משיגים את אובייקט SharedPreferences
        // ה-Context הוא בתוך ה-JobService
        SharedPreferences prefs = getSharedPreferences(MyConstants.SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        // שלב 2: פותחים את העורך (Editor) כדי לבצע שינויים
        SharedPreferences.Editor editor = prefs.edit();

        // שלב 3: שומרים את הנתון (Key: "LastNotifiedWeek", Value: weekNumber)
        editor.putInt(MyConstants.KEY_LAST_NOTIFIED_WEEK, weekNumber);

        // שלב 4: מפעילים את השמירה. Apply היא אסינכרונית (מהירה, לא חוסמת) ומומלצת ב-JobService.
        editor.apply();
    }
    private void createNotificationChannel(String channelId) {
        // בודק אם גרסת המערכת היא Oreo ומעלה (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // הגדרת מאפייני הערוץ
            CharSequence name = "תזכורות הריון שבועיות";
            String description = "התראות על כניסה לשבוע הריון חדש ומידע רלוונטי.";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // יצירת הערוץ
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            // רישום הערוץ במערכת
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    private void sendPushNotification(String message) {
        // 1. הגדרת משתנים
        String CHANNEL_ID = "WEEKLY_REMINDER_CHANNEL";
        int NOTIFICATION_ID = 1; // ID ייחודי לכל התראה

        // 2. יצירת ערוץ התראות (חובה ל-API 26 ומעלה)
        createNotificationChannel(CHANNEL_ID);

        // 3. הגדרת כוונת (Intent) לפתיחת האפליקציה לאחר לחיצה
        Intent intent = new Intent(this, WeeklyUpdateActivity.class); // נניח שיש לך Activity ראשי בשם MainActivity
        // כדי לוודא שכל ההתראות לא משתמשות באותה כוונה:
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // מעטפת ה-Intent:
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT // דגל חשוב לגרסאות חדשות
        );

        // 4. בניית ההתראה (ה-Notification)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_familly) // 💡 נניח שיצרתם אייקון שנקרא ic_notification
                .setContentTitle("🎉 בשורות טובות: שבוע היריון חדש!")
                .setContentText(message) // התוכן הוא ההודעה ששלחת לפונקציה (שבוע 10 וכו')
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // כדי להציג הודעה ארוכה
                .setPriority(NotificationCompat.PRIORITY_HIGH) // הופך את ההתראה לבהולה יותר
                .setContentIntent(pendingIntent) // מה קורה כשלוחצים עליה
                .setAutoCancel(true); // ההתראה נעלמת אחרי הלחיצה

        // 5. שליחת ההתראה
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
