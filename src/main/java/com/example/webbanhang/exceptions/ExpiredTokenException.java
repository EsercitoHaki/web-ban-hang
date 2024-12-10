package com.example.webbanhang.exceptions;

public class ExpiredTokenException extends Exception {
    public ExpiredTokenException(String message) {
        super(message);
    }
}
