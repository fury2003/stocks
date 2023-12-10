package com.stock.cashflow.exception;

public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String exception;

    public BadRequestException(String exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + exception;
    }

    @Override
    public String getMessage() {
        return exception;
    }
}
