package com.ysalu.web;

/**
 * Thrown when a protected API is accessed without a valid authenticated session.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
