package com.practice.shared_payment_backend.restservice.models.responses.payment;

import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;

import java.util.List;

public class PaymentListResponse extends AbstractResponse {

    private List<PaymentResponse> payments;

    public PaymentListResponse() {
    }

    public PaymentListResponse(List<PaymentResponse> payments) {
        this.payments = payments;
    }

    public List<PaymentResponse> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentResponse> payments) {
        this.payments = payments;
    }
}
