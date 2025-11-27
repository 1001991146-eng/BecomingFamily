package com.example.becomingfamily;

import java.util.Date;

public class User {

    private  String fullName;
    private  String email;
    private  String uid;
    private  LastPeriodDate lastPeriodDate;
    private  String role;
    private  EstimatedDate estimatedDate;
    public  String phone;
    public User()
    {

    }
    public User(String fullName, String email, String uid, LastPeriodDate lastPeriodDate, String role,EstimatedDate estimatedDate, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.uid = uid;
        this.lastPeriodDate = lastPeriodDate;
        this.role = role;
        this.estimatedDate=estimatedDate;
        this.phone=phone;
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

    public LastPeriodDate getLastPeriodDate() {
        return lastPeriodDate;
    }
    public EstimatedDate getEstimatedDate() {
        return estimatedDate;
    }

    public void setLastPeriodDate(LastPeriodDate lastPeriodDate) {
        this.lastPeriodDate = lastPeriodDate;
    }
    public void setEstimatedDate(EstimatedDate estimatedDate) {
        this.estimatedDate = estimatedDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {

        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", phone='" + phone +'\'' +
                ", email='" + email + '\'' +
                ", uid=" + uid +
                ", lastPeriodDate=" + lastPeriodDate +
                ", role='" + role + '\'' +
                '}';
    }
}
