package com.example.becomingfamily;

public class CurrentData {
    public static int weeks;
    public static String email;
    private static String role;
    public CurrentData(int weeks,String email)
    {
        this.weeks=weeks;
        this.email=email;
        this.role="Mom";
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
    public String GetRole()
    {
        return role;
    }
    public void SetWeeks(int weeks)
    {
        this.weeks=weeks;
    }
    public void SetEmail(String email)
    {
        this.email=email;
    }
    public void SetRole(String role)
    {
        this.role=role;
    }
    @Override
    public String toString() {
        return "CurrentData: email "+email+",weeks "+Integer.toString(weeks)+", role: "+role;
    }
}
