package com.practice.shared_payment_backend.restservice.models.requests;

import com.practice.shared_payment_backend.restservice.models.common.AbstractRequest;

import java.util.Set;

public class GroupRequest extends AbstractRequest {

    private String name;
    private String description;
    private Set<String> friends;

    public GroupRequest() {
    }

    public GroupRequest(String name, String description, Set<String> friends) {
        this.name = name;
        this.description = description;
        this.friends = friends;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getFriends() {
        return friends;
    }

    public void setFriends(Set<String> friends) {
        this.friends = friends;
    }
}
