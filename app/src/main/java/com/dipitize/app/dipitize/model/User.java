package com.dipitize.app.dipitize.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String email;
    public String fullName;
    public String username;
    public String profilePictureLink;
    public boolean appliedForJudge;
    public String phoneNumber;
    public long balance;
    public String bankName;
    public String accountType;
    public String accountNumber;
    public String fcmId;
    public boolean isBlocked;

    public User() {

    }

    public User(String email, String fullName, String username, String phoneNumber, long balance) {
        this.email = email;
        this.fullName = fullName;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.balance = balance;
        this.isBlocked = false;
    }

    public User(String email, String fullName, String username, String profilePictureLink, String phoneNumber, long balance) {
        this.email = email;
        this.fullName = fullName;
        this.username = username;
        this.profilePictureLink = profilePictureLink;
        this.phoneNumber = phoneNumber;
        this.balance = balance;
        this.isBlocked = false;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("fullName", fullName);
        result.put("username", username);
        result.put("profilePictureLink", profilePictureLink);
        result.put("appliedForJudge", appliedForJudge);
        result.put("phoneNumber", phoneNumber);
        result.put("balance", balance);
        result.put("bankName", bankName);
        result.put("accountNumber", accountNumber);
        result.put("accountType", accountType);
        result.put("isBlocked", isBlocked);
        result.put("fcmId", fcmId);

        return result;
    }
}
