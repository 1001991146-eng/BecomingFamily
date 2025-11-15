package com.example.becomingfamily;

public class LastPeriodDate {
    private int day;


    private int month;
    private  int year;
    // יש להוסיף בנאי ריק (חשוב מאוד ל-Firebase)
    public LastPeriodDate() {

    }
    public LastPeriodDate(int day, int month, int year) {
        this.day=day;
        this.month=month;
        this.year=year;
    }
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public int getMonth() {
        return month;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    @Override
    public String toString() {
        return "LastPeriodDate{" +
                "day=" + day +
                ", month=" + month +
                ", year=" + year +
                '}';
    }

}
