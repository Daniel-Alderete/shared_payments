package com.practice.shared_payment_backend.restservice.models.responses.group.info;

import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;

public class AmountResponse extends AbstractResponse {

    private String friendId;
    private float amount;

    public AmountResponse() {
    }

    public AmountResponse(String friendId, float amount) {
        this.friendId = friendId;
        this.amount = amount;
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
}
