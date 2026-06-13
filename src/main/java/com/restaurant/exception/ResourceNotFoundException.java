package com.restaurant.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entity, Long id) {
        super(entity + " with id " + id + " not found");
    }

    public ResourceNotFoundException(String entity, String field, Object value) {
        super(entity + " with " + field + " '" + value + "' not found");
    }
}
