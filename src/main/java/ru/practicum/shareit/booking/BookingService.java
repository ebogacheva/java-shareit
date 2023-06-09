package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.BookingsState;

import java.util.List;

public interface BookingService {

    BookingFullDto create(BookingInputDto bookingInputDto, Long userId);

    BookingFullDto getById(Long userId, Long bookingId);

    List<BookingFullDto> findUserBookings(Long userId, BookingsState state);

    List<BookingFullDto> findUserItemsBookings(Long userId, BookingsState state);

    BookingFullDto setStatus(Long userId, Long bookingId, boolean status);
}
