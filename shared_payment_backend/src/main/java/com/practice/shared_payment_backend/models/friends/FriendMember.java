package com.practice.shared_payment_backend.models.friends;

import com.practice.shared_payment_backend.models.common.AbstractFriend;

import java.util.Set;

public class FriendMember extends AbstractFriend {
    public FriendMember() {
    }

    public FriendMember(String id, String name, String surname, Set<String> payments) {
        super(id, name, surname, payments);
    }
}
