package com.example.becomingfamily;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WeekCalculator {
    private final int week;
    private final int days;

    public WeekCalculator(LastPeriodDate last) {
        if (last == null) {
            this.week = 1;
            this.days = 0;
        } else {
            Calendar lastPeriodCal = Calendar.getInstance();
            lastPeriodCal.set(last.getYear(), last.getMonth() - 1, last.getDay(), 0, 0, 0);
            lastPeriodCal.set(Calendar.MILLISECOND, 0);
            
            long diffInDays = calculateDiffInDays(lastPeriodCal.getTimeInMillis());
            
            if (diffInDays < 0) {
                this.week = 1;
                this.days = 0;
            } else {
                this.week = (int) (diffInDays / 7);
                this.days = (int) (diffInDays % 7);
            }
        }
    }

    public WeekCalculator(long lmpMillis) {
        long diffInDays = calculateDiffInDays(lmpMillis);
        if (diffInDays < 0) {
            this.week = 1;
            this.days = 0;
        } else {
            this.week = (int) (diffInDays / 7);
            this.days = (int) (diffInDays % 7);
        }
    }

    private long calculateDiffInDays(long lmpMillis) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long diffInMillies = today.getTimeInMillis() - lmpMillis;
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public int getWeek() {
        return week;
    }

    public int getDays() {
        return days;
    }
}
