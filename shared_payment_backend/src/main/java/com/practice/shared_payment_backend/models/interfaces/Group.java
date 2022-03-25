package com.practice.shared_payment_backend.models.interfaces;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

public interface Group {

    String getId();

    void setId(String id);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    Set<String> getFriends();

    void setFriends(Set<String> friends);

    JsonNode asJson();
}
