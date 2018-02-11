package com.dipitize.app.dipitize.model;

/**
 * Made by acefalobi on 3/16/2017.
 */

public class WithdrawRequest {
    public String user;
    public long amount;

    public WithdrawRequest() {

    }

    public WithdrawRequest(String user, long amount) {
        this.user = user;
        this.amount = amount;
    }
}
