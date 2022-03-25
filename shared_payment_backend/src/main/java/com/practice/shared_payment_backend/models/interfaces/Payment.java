package com.practice.shared_payment_backend.models.interfaces;

import com.fasterxml.jackson.databind.JsonNode;

public interface Payment {

    String getId();

    void setId(String id);

    float getAmount();

    void setAmount(float amount);

    String getDescription();

    void setDescription(String description);

    int getDate();

    void setDate(int date);

    JsonNode asJson();
}
