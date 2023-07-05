package ru.practicum.shareit.booking.controller;

import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final String BOOKER = "_FOR_BOOKER";
    private static final String OWNER = "_FOR_OWNER";

    private final BookingServiceImpl bookingService;

    @PostMapping
    public BookingFullDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                 @RequestBody BookingInputDto bookingInputDto) {
        return bookingService.create(bookingInputDto, userId);
    }

    @PatchMapping(value = "/{bookingId}")
    public BookingFullDto update(@RequestHeader(X_SHARER_USER_ID) long userId,
                                 @PathVariable Long bookingId,
                                 @RequestParam Boolean approved) {
        return bookingService.setStatus(userId, bookingId, approved);
    }

    @GetMapping(value = "/{bookingId}")
    public BookingFullDto getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                  @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingFullDto> findBookingsForBooker(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                      @RequestParam(name = "state", required = false, defaultValue = "") String searchCondition,
                                                      @RequestParam(required = false, defaultValue = "0") int from,
                                                      @RequestParam(required = false, defaultValue = "10") int size) {
        return bookingService.findBookings(userId, searchCondition, BOOKER, from, size);
    }

    @GetMapping(value = "/owner")
    public List<BookingFullDto> findBookingsForOwner(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                     @RequestParam(name = "state", required = false)  String searchCondition,
                                                     @RequestParam(required = false, defaultValue = "0") int from,
                                                     @RequestParam(required = false, defaultValue = "10") int size) {
        return bookingService.findBookings(userId, searchCondition, OWNER, from, size);
    }

}
