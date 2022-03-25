package com.practice.shared_payment_backend.models.friends;

import com.practice.shared_payment_backend.models.common.AbstractGroup;

import java.util.Set;

public class FriendGroup extends AbstractGroup {
    public FriendGroup() {
        super();
    }

    public FriendGroup(String id, String name, String description, Set<String> friends) {
        super(id, name, description, friends);
    }
}
