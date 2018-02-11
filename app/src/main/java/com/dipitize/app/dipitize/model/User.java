package com.dipitize.app.dipitize.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    public java.lang.String email;
    public java.lang.String fullName;
    public java.lang.String username;
    public java.lang.String profilePictureLink;
    public boolean appliedForJudge;

    public User() {

    }

    public User(java.lang.String email, java.lang.String fullName, java.lang.String username) {
        this.email = email;
        this.fullName = fullName;
        this.username = username;
    }

    public User(java.lang.String email, java.lang.String fullName, java.lang.String username, java.lang.String profilePictureLink) {
        this.email = email;
        this.fullName = fullName;
        this.username = username;
        this.profilePictureLink = profilePictureLink;
    }

    @Exclude
    public Map<java.lang.String, Object> toMap() {
        HashMap<java.lang.String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("fullName", fullName);
        result.put("username", username);
        result.put("profilePictureLink", profilePictureLink);
        result.put("appliedForJudge", appliedForJudge);

        return result;
    }
}
