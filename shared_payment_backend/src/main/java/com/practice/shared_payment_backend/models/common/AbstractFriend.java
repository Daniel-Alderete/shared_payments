package com.practice.shared_payment_backend.models.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import org.springframework.data.annotation.Id;

import java.util.Set;

public abstract class AbstractFriend implements Friend {

    @Id
    protected String id;
    protected String name;
    protected String surname;
    protected Set<String> payments;

    public AbstractFriend() {
    }

    public AbstractFriend(String id, String name, String surname, Set<String> payments) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.payments = payments;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public Set<String> getPayments() {
        return payments;
    }

    @Override
    public void setPayments(Set<String> payments) {
        this.payments = payments;
    }

    @Override
    public JsonNode asJson() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).valueToTree(this);
    }

    @Override
    public String toString() {
        return asJson().toString();
    }

}
