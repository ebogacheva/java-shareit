package ru.practicum.shareit.exception;

public class BookingIsAlreadyApprovedException extends RuntimeException {
    public BookingIsAlreadyApprovedException(String s) {
        super(s);
    }
}
