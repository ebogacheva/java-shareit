package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum SearchCondition {

	ALL,
	CURRENT,
	PAST,
	FUTURE,
	WAITING,
	REJECTED;

	public static Optional<SearchCondition> from(String stringState) {
		for (SearchCondition state : values()) {
			if (state.name().equalsIgnoreCase(stringState)) {
				return Optional.of(state);
			}
		}
		return Optional.empty();
	}
}
