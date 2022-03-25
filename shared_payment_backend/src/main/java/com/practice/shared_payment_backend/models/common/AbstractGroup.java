package com.practice.shared_payment_backend.models.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.shared_payment_backend.models.interfaces.Group;
import org.springframework.data.annotation.Id;

import java.util.Set;

public abstract class AbstractGroup implements Group {

    @Id
    protected String id;
    protected String name;
    protected String description;
    protected Set<String> friends;

    public AbstractGroup() {
    }

    public AbstractGroup(String id, String name, String description, Set<String> friends) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.friends = friends;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Set<String> getFriends() {
        return friends;
    }

    @Override
    public void setFriends(Set<String> friends) {
        this.friends = friends;
    }

    @Override
    public JsonNode asJson() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).valueToTree(this);
    }

    @Override
    public String toString() {
        return asJson().toString();
    }

}
