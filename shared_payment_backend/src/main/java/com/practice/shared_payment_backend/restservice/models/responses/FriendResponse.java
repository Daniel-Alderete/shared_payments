package com.practice.shared_payment_backend.restservice.models.responses;

import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;

import java.util.HashSet;
import java.util.Set;

public class FriendResponse extends AbstractResponse {

    private String id;
    private String name;
    private String surname;
    private Set<PaymentResponse> payments;

    public FriendResponse() {
    }

    public FriendResponse(String id, String name, String surname, Set<PaymentResponse> payments) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.payments = payments;
    }

    public FriendResponse(Friend friend, Set<Payment> payments) {
        this.id = friend.getId();
        this.name = friend.getName();
        this.surname = friend.getSurname();
        Set<PaymentResponse> responses = new HashSet<>();

        for (Payment payment : payments) {
            responses.add(new PaymentResponse(payment));
        }

        this.payments = responses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Set<PaymentResponse> getPayments() {
        return payments;
    }

    public void setPayments(Set<PaymentResponse> payments) {
        this.payments = payments;
    }
}
