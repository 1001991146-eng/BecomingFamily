package com.example.becomingfamily;

import java.util.Date;

public class User {
    private static String fullName;
    private static String email;
    private static String uid;
    private static Date lastPeriodDate;
    private static String role;
    private static Date estimatedDate;

    public User()
    {

    }
    public User(String fullName, String email, String uid, Date lastPeriodDate, String role,Date estimatedDate) {
        this.fullName = fullName;
        this.email = email;
        this.uid = uid;
        this.lastPeriodDate = lastPeriodDate;
        this.role = role;
        this.estimatedDate=estimatedDate;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getLastPeriodDate() {
        return lastPeriodDate;
    }
    public Date getEstimatedDate() {
        return estimatedDate;
    }

    public void setLastPeriodDate(Date lastPeriodDate) {
        this.lastPeriodDate = lastPeriodDate;
    }
    public void setEstimatedDate(Date estimatedDate) {
        this.estimatedDate = estimatedDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", uid=" + uid +
                ", lastPeriodDate=" + lastPeriodDate +
                ", role='" + role + '\'' +
                '}';
    }
}
