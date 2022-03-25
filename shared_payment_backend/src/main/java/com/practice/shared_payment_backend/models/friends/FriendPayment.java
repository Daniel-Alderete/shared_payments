package com.practice.shared_payment_backend.models.friends;

import com.practice.shared_payment_backend.models.common.AbstractPayment;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payments")
public class FriendPayment extends AbstractPayment {
    public FriendPayment() {
        super();
    }

    public FriendPayment(String id, float amount, String description, int date) {
        super(id, amount, description, date);
    }
}
