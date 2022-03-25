package com.practice.shared_payment_backend.repository;

import com.practice.shared_payment_backend.models.friends.FriendMember;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends MongoRepository<FriendMember, String> {

    Friend findByNameAndSurname(String name, String surname);
}
