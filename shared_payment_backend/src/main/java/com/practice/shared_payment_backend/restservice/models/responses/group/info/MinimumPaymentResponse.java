package com.practice.shared_payment_backend.restservice.models.responses.group.info;

import java.util.List;

public class MinimumPaymentResponse {

    private String friendId;
    private List<AmountResponse> payments;

    public MinimumPaymentResponse() {
    }

    public MinimumPaymentResponse(String friendId, List<AmountResponse> payments) {
        this.friendId = friendId;
        this.payments = payments;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public List<AmountResponse> getPayments() {
        return payments;
    }

    public void setPayments(List<AmountResponse> payments) {
        this.payments = payments;
    }
}
