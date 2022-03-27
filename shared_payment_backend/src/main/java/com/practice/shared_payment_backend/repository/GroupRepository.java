package com.practice.shared_payment_backend.repository;

import com.practice.shared_payment_backend.models.friends.FriendGroup;
import com.practice.shared_payment_backend.models.interfaces.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface GroupRepository extends MongoRepository<FriendGroup, String> {

    Group findByFriends(Set<String> friends);
}
