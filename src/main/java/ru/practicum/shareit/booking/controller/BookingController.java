package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final String BOOKER = "_FOR_BOOKER";
    private static final String OWNER = "_FOR_OWNER";
    private final BookingService bookingServiceImpl;

    @PostMapping
    public BookingFullDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                 @Valid @RequestBody BookingInputDto bookingInputDto) {
        return bookingServiceImpl.create(bookingInputDto, userId);
    }

    @PatchMapping(value = "/{bookingId}")
    public BookingFullDto update(@RequestHeader(X_SHARER_USER_ID) long userId,
                                 @Min(0) @PathVariable Long bookingId,
                                 @NotNull @RequestParam Boolean approved) {
        return bookingServiceImpl.setStatus(userId, bookingId, approved);
    }

    @GetMapping(value = "/{bookingId}")
    public BookingFullDto getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                  @Min(0) @PathVariable Long bookingId) {
        return bookingServiceImpl.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingFullDto> findBookingsForBooker(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                      @RequestParam(name = "state", required = false, defaultValue = "") String searchCondition,
                                                      @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                                      @Min(0) @RequestParam(required = false, defaultValue = "10") int size) {
        return bookingServiceImpl.findBookings(userId, searchCondition, BOOKER, from, size);
    }

    @GetMapping(value = "/owner")
    public List<BookingFullDto> findBookingsForOwner(@RequestHeader(X_SHARER_USER_ID) long userId,
                                              @RequestParam(name = "state", required = false)  String searchCondition,
                                              @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                              @Min(0) @RequestParam(required = false, defaultValue = "10") int size) {
        return bookingServiceImpl.findBookings(userId, searchCondition, OWNER, from, size);
    }

}
