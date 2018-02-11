package com.dipitize.app.dipitize.model;

public class Challenge {
    public long amount;
    public String mediaUrl;
    public User challenger;
    public String challengerId;
    public String category;
    public long timeStarted;

    public Challenge() {

    }

    public Challenge(long amount, String mediaUrl, String challengerId, User challenger, String category) {
        this.amount = amount;
        this.mediaUrl = mediaUrl;
        this.challengerId = challengerId;
        this.challenger = challenger;
        this.category = category;
        this.timeStarted = System.currentTimeMillis();
    }
}
