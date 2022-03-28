package com.practice.shared_payment_backend.restservice.common;

import com.practice.shared_payment_backend.restservice.models.exceptions.BadRequestBodyException;
import com.practice.shared_payment_backend.restservice.models.exceptions.FriendNotFoundException;
import com.practice.shared_payment_backend.restservice.models.exceptions.GroupNotFoundException;
import com.practice.shared_payment_backend.restservice.models.exceptions.PaymentNotFoundException;
import com.practice.shared_payment_backend.restservice.models.responses.ApiErrorResponse;
import com.practice.shared_payment_backend.restservice.models.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(GroupNotFoundException.class)
    @ResponseBody
    public ApiResponse groupNotFound() {
        return new ApiResponse(new ApiErrorResponse(101, "Group not found"));
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(FriendNotFoundException.class)
    @ResponseBody
    public ApiResponse friendNotFound() {
        return new ApiResponse(new ApiErrorResponse(102, "Friend not found"));
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseBody
    public ApiResponse paymentNotFound() {
        return new ApiResponse(new ApiErrorResponse(103, "Payment not found"));
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestBodyException.class)
    @ResponseBody
    public ApiResponse badRequestBody() {
        return new ApiResponse(new ApiErrorResponse(104, "Missing at least one parameter in the request"));
    }
}
