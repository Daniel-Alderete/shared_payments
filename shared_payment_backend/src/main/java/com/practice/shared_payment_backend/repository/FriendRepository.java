package com.practice.shared_payment_backend.repository;

import com.practice.shared_payment_backend.models.friends.FriendMember;
import com.practice.shared_payment_backend.models.interfaces.Friend;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface FriendRepository extends MongoRepository<FriendMember, String> {

    Friend findByNameAndSurname(String name, String surname);

    Friend findByPayments(Set<String> payments);
}
