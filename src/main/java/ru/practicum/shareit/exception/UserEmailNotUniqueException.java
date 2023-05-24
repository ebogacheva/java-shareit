package ru.practicum.shareit.exception;

public class UserEmailNotUniqueException extends RuntimeException {
    public UserEmailNotUniqueException(String s) {
        super(s);
    }
}
