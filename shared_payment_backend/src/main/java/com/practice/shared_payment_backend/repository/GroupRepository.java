package com.practice.shared_payment_backend.repository;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.interfaces.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Set;

public interface GroupRepository extends MongoRepository<FriendGroup, String> {

    Group findByFriends(Set<String> friends);
}
