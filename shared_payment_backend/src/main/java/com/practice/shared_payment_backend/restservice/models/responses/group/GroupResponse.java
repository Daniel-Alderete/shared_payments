package com.practice.shared_payment_backend.restservice.models.responses.group;

import com.practice.shared_payment_backend.models.interfaces.Friend;
import com.practice.shared_payment_backend.models.interfaces.Group;
import com.practice.shared_payment_backend.models.interfaces.Payment;
import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;
import com.practice.shared_payment_backend.restservice.models.responses.friend.FriendResponse;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupResponse extends AbstractResponse {

    private String id;
    private String name;
    private String description;
    private Set<FriendResponse> friends;

    public GroupResponse() {
    }

    public GroupResponse(String id, String name, String description, Set<FriendResponse> friends) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.friends = friends;
    }

    public GroupResponse(Group group, Set<Friend> friends) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();

        Set<FriendResponse> responses = new HashSet<>();

        for (Friend friend : friends) {
            responses.add(new FriendResponse(friend));
        }

        this.friends = responses;
    }

    public GroupResponse(Group group, Map<Friend, Set<Payment>> friendPaymentMap) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        Set<FriendResponse> friendResponses = new HashSet<>();

        for (Friend friend : friendPaymentMap.keySet()) {
            friendResponses.add(new FriendResponse(friend, friendPaymentMap.get(friend)));
        }

        this.friends = friendResponses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Set<FriendResponse> getFriends() {
        return friends;
    }

    public void setFriends(Set<FriendResponse> friends) {
        this.friends = friends;
    }
}
