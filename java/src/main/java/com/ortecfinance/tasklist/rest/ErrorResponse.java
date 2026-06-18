package com.ortecfinance.tasklist.rest;

/** Consistent error body returned for failed REST requests. */
public record ErrorResponse(int status, String error, String message, String path) {
}
