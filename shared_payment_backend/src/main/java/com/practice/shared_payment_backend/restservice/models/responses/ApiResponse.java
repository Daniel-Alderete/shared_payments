package com.practice.shared_payment_backend.restservice.models.responses;

import com.practice.shared_payment_backend.restservice.models.common.AbstractJsonBody;
import com.practice.shared_payment_backend.restservice.models.interfaces.DataResponse;
import com.practice.shared_payment_backend.restservice.models.interfaces.ErrorResponse;

public class ApiResponse extends AbstractJsonBody {

    private DataResponse data;
    private ErrorResponse error;

    public ApiResponse() {
    }

    public ApiResponse(DataResponse data, ErrorResponse error) {
        this.data = data;
        this.error = error;
    }

    public ApiResponse(DataResponse data) {
        this.data = data;
        this.error = null;
    }

    public ApiResponse(ErrorResponse error) {
        this.data = null;
        this.error = error;
    }

    public DataResponse getData() {
        return data;
    }

    public void setData(DataResponse data) {
        this.data = data;
    }

    public ErrorResponse getError() {
        return error;
    }

    public void setError(ErrorResponse error) {
        this.error = error;
    }
}
