package com.dipitize.app.dipitize.model;

/**
 * Made by acefalobi on 3/31/2017.
 */

public class Message {
    public String userId;
    public User user;
    public String message;
    public long timeSent;

    public Message() {

    }

    public Message(String userId, User user, String message) {
        this.userId = userId;
        this.user = user;
        this.message = message;
        this.timeSent = System.currentTimeMillis();
    }
}
