package com.dipitize.app.dipitize.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Made by acefalobi on 3/31/2017.
 */

public class MessageThread {

    public User user;
    public List<Message> messages;

    public MessageThread() {

    }

    public MessageThread(User user, List<Message> messages) {
        this.user = user;
        this.messages = messages;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("messages", messages);

        return result;
    }
}
