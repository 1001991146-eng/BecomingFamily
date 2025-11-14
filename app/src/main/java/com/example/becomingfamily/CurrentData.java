package com.example.becomingfamily;

public class CurrentData {
    public static int weeks;
    public static String email;
    public CurrentData(int weeks,String email)
    {
        this.weeks=weeks;
        this.email=email;
    }
    public CurrentData()
    {

    }

    public int GetWeeks()
    {
        return weeks;
    }
    public String GetEmail()
    {
        return email;
    }
    public void SetWeeks(int weeks)
    {
        this.weeks=weeks;
    }
    public void SetEmail(String email)
    {
        this.email=email;
    }

    @Override
    public String toString() {
        return "CurrentData: email "+email+",weeks "+Integer.toString(weeks);
    }
}
