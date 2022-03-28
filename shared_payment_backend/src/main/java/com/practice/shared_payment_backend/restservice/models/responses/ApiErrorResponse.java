package com.practice.shared_payment_backend.restservice.models.responses;

import com.practice.shared_payment_backend.restservice.models.common.AbstractJsonBody;
import com.practice.shared_payment_backend.restservice.models.interfaces.ErrorResponse;

public class ApiErrorResponse extends AbstractJsonBody implements ErrorResponse {

    private int code;
    private String message;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
