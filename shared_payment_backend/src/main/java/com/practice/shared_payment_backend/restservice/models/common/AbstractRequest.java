package com.practice.shared_payment_backend.restservice.models.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractRequest {

    public JsonNode asJson() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).valueToTree(this);
    }

    @Override
    public String toString() {
        return asJson().toString();
    }
}
