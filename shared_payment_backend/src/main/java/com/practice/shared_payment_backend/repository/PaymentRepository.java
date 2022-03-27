package com.practice.shared_payment_backend.repository;

import com.practice.shared_payment_backend.models.friends.FriendPayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<FriendPayment, String> {
}
