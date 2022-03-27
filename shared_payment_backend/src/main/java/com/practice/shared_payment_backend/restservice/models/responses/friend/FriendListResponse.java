package com.practice.shared_payment_backend.restservice.models.responses.friend;

import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;

import java.util.List;

public class FriendListResponse extends AbstractResponse {

    private List<FriendResponse> friends;

    public FriendListResponse() {
    }

    public FriendListResponse(List<FriendResponse> friends) {
        this.friends = friends;
    }

    public List<FriendResponse> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendResponse> friends) {
        this.friends = friends;
    }
}
