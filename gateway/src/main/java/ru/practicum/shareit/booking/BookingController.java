package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.SearchCondition;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) long userId,
										 @Valid @RequestBody BookingInputDto bookingInputDto) {
		return bookingClient.create(bookingInputDto, userId);
	}

	@PatchMapping(value = "/{bookingId}")
	public ResponseEntity<Object> update(@RequestHeader(X_SHARER_USER_ID) long userId,
										 @Min(0) @PathVariable Long bookingId,
										 @NotNull @RequestParam Boolean approved) {
		return bookingClient.update(userId, bookingId, approved);
	}

	@GetMapping(value = "/{bookingId}")
	public ResponseEntity<Object> getById(@RequestHeader(X_SHARER_USER_ID) long userId,
										  @Min(0) @PathVariable Long bookingId) {
		return bookingClient.getById(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> findBookingsForBooker(@RequestHeader(X_SHARER_USER_ID) long userId,
														@RequestParam(name = "state", required = false, defaultValue = "all") String state,
														@Min(0) @RequestParam(required = false, defaultValue = "0") int from,
														@Min(1) @RequestParam(required = false, defaultValue = "10") int size) {
		SearchCondition searchCondition = SearchCondition.from(state)
				.orElseThrow(() -> new UnsupportedStatusException(state));
		return bookingClient.getBookingsForBooker(userId, searchCondition, from, size);
	}

	@GetMapping(value = "/owner")
	public ResponseEntity<Object> findBookingsForOwner(@RequestHeader(X_SHARER_USER_ID) long userId,
													   @RequestParam(name = "state", required = false, defaultValue = "all")  String state,
													   @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
													   @Min(1) @RequestParam(required = false, defaultValue = "10") int size) {
		SearchCondition searchCondition = SearchCondition.from(state)
				.orElseThrow(() -> new UnsupportedStatusException(state));
		return bookingClient.getBookingsForOwner(userId, searchCondition, from, size);
	}
}
