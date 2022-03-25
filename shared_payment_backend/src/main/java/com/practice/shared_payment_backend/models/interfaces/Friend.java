package com.practice.shared_payment_backend.models.interfaces;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

public interface Friend {

    String getId();

    void setId(String id);

    String getName();

    void setName(String name);

    String getSurname();

    void setSurname(String surname);

    Set<String> getPayments();

    void setPayments(Set<String> payments);

    JsonNode asJson();
}
