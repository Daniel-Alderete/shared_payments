package com.practice.shared_payment_backend.restservice.common;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Not Found")
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public void notFound() {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad Request")
    @ExceptionHandler(DataRetrievalFailureException.class)
    public void badRequest() {
    }
}