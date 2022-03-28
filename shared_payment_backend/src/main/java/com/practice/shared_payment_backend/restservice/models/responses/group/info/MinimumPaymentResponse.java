package com.practice.shared_payment_backend.restservice.models.responses.group.info;

import java.util.List;

public class MinimumPaymentResponse {

    private String friendId;
    private String friendName;
    private String friendSurname;
    private List<AmountResponse> payments;

    public MinimumPaymentResponse() {
    }

    public MinimumPaymentResponse(String friendId, String friendName, String friendSurname, List<AmountResponse> payments) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.friendSurname = friendSurname;
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
