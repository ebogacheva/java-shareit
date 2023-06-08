package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingsState;

import java.util.List;

public interface BookingService {

    BookingDto create(BookingDto bookingDto, Long userId);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> findUserBookings(Long userId, BookingsState state);

    List<BookingDto> findUserItemsBookings(Long userId, BookingsState state);

    BookingDto setStatus(Long userId, Long bookingId, boolean status);
}
