package com.stock.cashflow.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    @ExceptionHandler({BadRequestException.class})
    @ResponseBody
    public ResponseEntity<?> handleAnyException(Exception e) {
        log.error(e.getMessage());
        Throwable throwable = new Throwable(e.getMessage());
        return errorResponse(throwable, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ExceptionMessage> errorResponse(Throwable throwable, HttpStatus status) {

        if (null != throwable) {
            return response(new ExceptionMessage(throwable), status);
        } else {
            return response(null, status);
        }
    }

    private <T> ResponseEntity<T> response(T body, HttpStatus status) {
        return new ResponseEntity<>(body, new HttpHeaders(), status);
    }
}
