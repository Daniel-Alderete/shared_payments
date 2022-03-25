package com.practice.shared_payment_backend.models.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import org.springframework.data.annotation.Id;

public abstract class AbstractPayment implements Payment {

    @Id
    protected String id;
    protected float amount;
    protected String description;
    protected int date;

    public AbstractPayment() {
    }

    public AbstractPayment(String id, float amount, String description, int date) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
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
    public float getAmount() {
        return amount;
    }

    @Override
    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getDate() {
        return date;
    }

    @Override
    public void setDate(int date) {
        this.date = date;
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
