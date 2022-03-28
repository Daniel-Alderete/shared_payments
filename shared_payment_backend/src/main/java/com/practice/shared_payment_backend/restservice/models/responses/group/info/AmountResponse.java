package com.practice.shared_payment_backend.restservice.models.responses.group.info;

import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;

public class AmountResponse extends AbstractResponse {

    private String friendId;
    private String friendName;
    private String friendSurname;
    private float amount;

    public AmountResponse() {
    }

    public AmountResponse(String friendId, float amount, String friendName, String friendSurname) {
        this.friendId = friendId;
        this.amount = amount;
        this.friendName = friendName;
        this.friendSurname = friendSurname;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendSurname() {
        return friendSurname;
    }

    public void setFriendSurname(String friendSurname) {
        this.friendSurname = friendSurname;
    }
}
