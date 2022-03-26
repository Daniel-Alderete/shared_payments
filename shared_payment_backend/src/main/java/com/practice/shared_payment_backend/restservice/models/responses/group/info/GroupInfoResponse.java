package com.practice.shared_payment_backend.restservice.models.responses.group.info;

import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;

import java.util.List;

public class GroupInfoResponse extends AbstractResponse {

    private List<AmountResponse> debts;
    private List<MinimumPaymentResponse> minimumPayment;

    public GroupInfoResponse() {
    }

    public GroupInfoResponse(List<AmountResponse> debts, List<MinimumPaymentResponse> minimumPayment) {
        this.debts = debts;
        this.minimumPayment = minimumPayment;
    }

    public List<AmountResponse> getDebts() {
        return debts;
    }

    public void setDebts(List<AmountResponse> debts) {
        this.debts = debts;
    }

    public List<MinimumPaymentResponse> getMinimumPayment() {
        return minimumPayment;
    }

    public void setMinimumPayment(List<MinimumPaymentResponse> minimumPayment) {
        this.minimumPayment = minimumPayment;
    }
}
