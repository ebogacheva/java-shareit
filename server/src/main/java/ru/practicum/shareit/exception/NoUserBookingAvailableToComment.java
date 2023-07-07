package ru.practicum.shareit.exception;

public class NoUserBookingAvailableToComment extends RuntimeException {
    public NoUserBookingAvailableToComment(String s) {
        super(s);
    }
}
