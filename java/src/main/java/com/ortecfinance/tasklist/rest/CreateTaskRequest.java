package com.ortecfinance.tasklist.rest;

/** Request body for creating a task within a project. */
public record CreateTaskRequest(String description) {
}
