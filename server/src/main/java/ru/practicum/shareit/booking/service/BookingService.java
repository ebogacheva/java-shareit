package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import java.util.List;

public interface BookingService {

    BookingFullDto create(BookingInputDto bookingInputDto, Long userId);

    BookingFullDto getById(Long userId, Long bookingId);

    List<BookingFullDto> findBookings(Long userId, String state, String requester, int from, int size);

    BookingFullDto setStatus(Long userId, Long bookingId, boolean status);
}
