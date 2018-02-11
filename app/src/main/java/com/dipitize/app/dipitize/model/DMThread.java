package com.dipitize.app.dipitize.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Made by acefalobi on 5/10/2017.
 */

public class DMThread {
    public User user1;
    public User user2;
    public String userId1;
    public String userId2;
    public List<Message> messages;

    public DMThread() {

    }

    public DMThread(User user1, User user2, String userId1, String userId2,  List<Message> messages) {
        this.user1 = user1;
        this.user2 = user2;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.messages = messages;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("user1", user1);
        result.put("user2", user2);
        result.put("userId1", userId1);
        result.put("userId2", userId2);
        result.put("messages", messages);

        return result;
    }
}
