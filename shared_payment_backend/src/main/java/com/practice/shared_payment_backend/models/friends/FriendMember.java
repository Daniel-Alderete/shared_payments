package com.practice.shared_payment_backend.models.friends;

import com.practice.shared_payment_backend.models.common.AbstractFriend;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "friends")
public class FriendMember extends AbstractFriend {
    public FriendMember() {
        super();
    }

    public FriendMember(String name, String surname, Set<String> payments) {
        super(name, surname, payments);
    }

    public FriendMember(String id, String name, String surname, Set<String> payments) {
        super(id, name, surname, payments);
    }
}
