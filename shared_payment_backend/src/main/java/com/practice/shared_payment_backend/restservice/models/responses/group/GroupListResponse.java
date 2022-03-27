package com.practice.shared_payment_backend.restservice.models.responses.group;

import com.practice.shared_payment_backend.restservice.models.common.AbstractResponse;

import java.util.List;

public class GroupListResponse extends AbstractResponse {

    private List<GroupResponse> groups;

    public GroupListResponse() {
    }

    public GroupListResponse(List<GroupResponse> groups) {
        this.groups = groups;
    }

    public List<GroupResponse> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupResponse> groups) {
        this.groups = groups;
    }
}
