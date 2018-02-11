package com.dipitize.app.dipitize.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Made by acefalobi on 3/16/2017.
 */

public class Notification {
    public String receiver;
    public boolean isRead;
    public String message;

    public Notification() {}

    public Notification(String receiver, String message) {
        this.receiver = receiver;
        this.message = message;
        this.isRead = false;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("receiver", receiver);
        result.put("message", message);
        result.put("isRead", isRead);

        return result;
    }
}
