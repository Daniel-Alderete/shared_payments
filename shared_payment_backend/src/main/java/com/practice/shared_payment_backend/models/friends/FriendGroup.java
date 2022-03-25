package com.practice.shared_payment_backend.models.friends;

import com.practice.shared_payment_backend.models.common.AbstractGroup;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "groups")
public class FriendGroup extends AbstractGroup {
    public FriendGroup() {
        super();
    }

    public FriendGroup(String name, String description, Set<String> friends) {
        super(name, description, friends);
    }

    public FriendGroup(String id, String name, String description, Set<String> friends) {
        super(id, name, description, friends);
    }
}
