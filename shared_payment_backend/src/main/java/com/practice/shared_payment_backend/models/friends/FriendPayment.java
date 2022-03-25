package com.practice.shared_payment_backend.models.friends;

import com.practice.shared_payment_backend.models.common.AbstractPayment;

public class FriendPayment extends AbstractPayment {
    public FriendPayment() {
    }

    public FriendPayment(String id, float amount, String description, int date) {
        super(id, amount, description, date);
    }
}
