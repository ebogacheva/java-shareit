package ru.practicum.shareit.exception;

public class AccessForbiddenException extends RuntimeException {
    public AccessForbiddenException(String s) {
        super(s);
    }
}
