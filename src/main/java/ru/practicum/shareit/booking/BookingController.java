package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingsState;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingServiceImpl;

    @PostMapping
    public BookingDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                             @Valid @RequestBody BookingDto bookingDto) {
        return bookingServiceImpl.create(bookingDto, userId);
    }

    @PatchMapping(value = "/bookings/{bookingId}?approved={approved}")
    public BookingDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                          @Positive @PathVariable Long bookingId,
                          @NotNull @RequestParam Boolean approved) {
        return bookingServiceImpl.setStatus(userId, bookingId, approved);
    }

    @GetMapping(value = "/bookings/{bookingId}")
    public BookingDto getById(@RequestHeader("X-Sharer-User-Id") long userId,
                              @Positive @PathVariable Long bookingId) {
        return bookingServiceImpl.getById(userId, bookingId);
    }

    @GetMapping(value = "/bookings?state={state}")
    public List<BookingDto> findUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @RequestParam Optional<BookingsState> state) {
        BookingsState requestedState = state.orElse(BookingsState.ALL);
        return bookingServiceImpl.findUserBookings(userId, requestedState);
    }

    @GetMapping(value = "/bookings/owner?state={state}")
    public List<BookingDto> findUserItemsBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(name = "state") Optional<BookingsState> state) {
        BookingsState requestedState = state.orElse(BookingsState.ALL);
        return bookingServiceImpl.findUserItemsBookings(userId, requestedState);
    }

}
