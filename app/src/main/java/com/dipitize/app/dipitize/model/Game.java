package com.dipitize.app.dipitize.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {
    public long amount;
    public String winner;
    public String category;
    public long timeStarted;
    public boolean isFinished;
    public List<User> players;
    public List<String> playerIds;
    public List<String> mediaUrls;
    public List<String> voters1;
    public List<String> voters2;

    public Game() {

    }

    public Game(long amount, List<User> players, List<String> playerIds, List<String> mediaUrls, String category) {
        this.amount = amount;
        this.players = players;
        this.playerIds = playerIds;
        this.mediaUrls = mediaUrls;
        this.category = category;
        this.isFinished = false;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("amount", amount);
        result.put("winner", winner);
        result.put("timeStarted", timeStarted);
        result.put("isFinished", isFinished);
        result.put("players", players);
        result.put("playerIds", playerIds);
        result.put("mediaUrls", mediaUrls);
        result.put("voters1", voters1);
        result.put("voters2", voters2);
        result.put("category", category);

        return result;
    }
}
