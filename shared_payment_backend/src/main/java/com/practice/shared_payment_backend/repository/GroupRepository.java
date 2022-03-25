package com.practice.shared_payment_backend.repository;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<FriendGroup, String> {
}
