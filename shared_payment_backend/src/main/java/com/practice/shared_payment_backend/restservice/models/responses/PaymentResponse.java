package com.practice.shared_payment_backend.restservice.models.responses;

import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;

public class PaymentResponse extends AbstractResponse {

    private String id;
    private float amount;
    private String description;
    private long date;

    public PaymentResponse() {
    }

    public PaymentResponse(String id, float amount, String description, long date) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.amount = payment.getAmount();
        this.description = payment.getDescription();
        this.date = payment.getDate();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
