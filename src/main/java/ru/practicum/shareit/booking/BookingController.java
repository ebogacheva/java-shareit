package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.BookingsState;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Objects;

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
    public BookingFullDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                 @Valid @RequestBody BookingInputDto bookingInputDto) {
        return bookingServiceImpl.create(bookingInputDto, userId);
    }

    @PatchMapping(value = "/{bookingId}")
    public BookingFullDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @Positive @PathVariable Long bookingId,
                                 @NotNull @RequestParam Boolean approved) {
        return bookingServiceImpl.setStatus(userId, bookingId, approved);
    }

    @GetMapping(value = "/{bookingId}")
    public BookingFullDto getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Positive @PathVariable Long bookingId) {
        return bookingServiceImpl.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingFullDto> findUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(required = false) BookingsState state) {
        return bookingServiceImpl.findUserBookings(userId, checkAndSetIfNull(state));
    }

    @GetMapping(value = "/owner?state={state}")
    public List<BookingFullDto> findUserItemsBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                      @RequestParam(required = false)  BookingsState state) {
        return bookingServiceImpl.findUserItemsBookings(userId, checkAndSetIfNull(state));
    }

    private BookingsState checkAndSetIfNull(BookingsState state) {
        return Objects.isNull(state) ? BookingsState.ALL : state;
    }

}
