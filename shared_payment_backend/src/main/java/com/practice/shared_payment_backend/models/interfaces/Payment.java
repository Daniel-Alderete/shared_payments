package com.practice.shared_payment_backend.models.interfaces;

import com.fasterxml.jackson.databind.JsonNode;

public interface Payment {

    String getId();

    void setId(String id);

    float getAmount();

    void setAmount(float amount);

    String getDescription();

    void setDescription(String description);

    long getDate();

    void setDate(long date);

    JsonNode asJson();
}
