package ru.practicum.shareit.booking.model;

public enum BookingStatus {

    WAITING,
    APPROVED,
    REJECTED,
    CANCELED;

    public static BookingStatus getApprovedOrRejected(boolean approved) {
        if (approved) {
            return BookingStatus.APPROVED;
        } else {
            return BookingStatus.REJECTED;
        }
    }
}
