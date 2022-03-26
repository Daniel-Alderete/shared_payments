package com.practice.shared_payment_backend.restservice.models.requests;

import com.practice.shared_payment_backend.restservice.models.common.AbstractRequest;

public class PaymentRequest extends AbstractRequest {

    private Float amount;
    private String description;
    private Long date;

    public PaymentRequest() {
    }

    public PaymentRequest(Float amount, String description, Long date) {
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
