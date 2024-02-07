package com.melouk.personal.service;

public class NoRatesFoundException extends RuntimeException {
    public NoRatesFoundException(String message) {
        super(message);
    }
}
