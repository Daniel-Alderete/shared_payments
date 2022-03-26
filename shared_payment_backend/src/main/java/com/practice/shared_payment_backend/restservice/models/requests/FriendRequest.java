package com.practice.shared_payment_backend.restservice.models.requests;

import java.util.Set;

public class FriendRequest {

    private String name;
    private String surname;
    private Set<String> payments;

    public FriendRequest() {
    }

    public FriendRequest(String name, String surname, Set<String> payments) {
        this.name = name;
        this.surname = surname;
        this.payments = payments;
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

    public Set<String> getPayments() {
        return payments;
    }

    public void setPayments(Set<String> payments) {
        this.payments = payments;
    }
}
