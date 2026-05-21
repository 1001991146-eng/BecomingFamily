package com.example.becomingfamily;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WeekCalculator {
    private int week;
    private int days;
    private static Calendar calendar;
    private static Date lastPeriodDate;
    public WeekCalculator(LastPeriodDate last)
    {
        // 1. יצירת אובייקט Calendar
        Calendar lastPeriodCal = Calendar.getInstance();
        // 2. הגדרת התאריך הנכון:
        // הערה חשובה: חודשי Calendar הם 0-11, לכן יש להחסיר 1 ממה שמגיע מה-DB (1-12)
        int year = last.getYear();
        int month = last.getMonth() - 1; // התיקון העיקרי! אם החודש 1 (ינואר), הוא יהפוך ל-0.
        int day = last.getDay();
        // הגדרת התאריך ב-Calendar
        lastPeriodCal.set(year, month, day);
        // 3. חילוץ תאריכים
        lastPeriodDate = lastPeriodCal.getTime();
    }

    public int GetWeek()
    {
        Date lastPeriodDate = calendar.getTime();
        Date today = new Date();

        long diffInMillies = today.getTime() - lastPeriodDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        return (int) (diffInDays / 7) ;
    }
    public int GetDays()
    {
        Date lastPeriodDate = calendar.getTime();
        Date today = new Date();

        long diffInMillies = today.getTime() - lastPeriodDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        return (int) (diffInDays % 7) ;
    }


}
