package ru.practicum.shareit.booking.model;

import org.apache.logging.log4j.util.Strings;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum BookingsState {
    ALL {
        @Override
        public List<Booking> getUserBookings(BookingRepository repository, Long userId) {
            return repository.findAllUserBookings(userId);
        }

        @Override
        public List<Booking> getUserItemsBookings(BookingRepository repository, Long userId) {
            return repository.findAllUserItemsBookings(userId);
        }
    },
    CURRENT {
        @Override
        public List<Booking> getUserBookings(BookingRepository repository, Long userId) {
            return repository.findCurrentUserBookings(userId, LocalDateTime.now());
        }

        @Override
        public List<Booking> getUserItemsBookings(BookingRepository repository, Long userId) {
            return repository.findCurrentUserItemsBookings(userId, LocalDateTime.now());
        }
    },
    PAST {
        @Override
        public List<Booking> getUserBookings(BookingRepository repository, Long userId) {
            return repository.findPastUserBookings(userId, LocalDateTime.now());
        }

        @Override
        public List<Booking> getUserItemsBookings(BookingRepository repository, Long userId) {
            return repository.findPastUserItemsBookings(userId, LocalDateTime.now());
        }
    },
    FUTURE {
        @Override
        public List<Booking> getUserBookings(BookingRepository repository, Long userId) {
            return repository.findFutureUserBookings(userId, LocalDateTime.now());
        }

        @Override
        public List<Booking> getUserItemsBookings(BookingRepository repository, Long userId) {
            return repository.findFutureUserItemsBookings(userId, LocalDateTime.now());
        }
    },
    WAITING {
        @Override
        public List<Booking> getUserBookings(BookingRepository repository, Long userId) {
            return repository.findUserBookingsByStatus(userId, BookingStatus.WAITING);
        }

        @Override
        public List<Booking> getUserItemsBookings(BookingRepository repository, Long userId) {
            return repository.findUserItemsBookingsByStatus(userId, BookingStatus.WAITING);
        }
    },
    REJECTED {
        @Override
        public List<Booking> getUserBookings(BookingRepository repository, Long userId) {
            return repository.findUserBookingsByStatus(userId, BookingStatus.REJECTED);
        }

        @Override
        public List<Booking> getUserItemsBookings(BookingRepository repository, Long userId) {
            return repository.findUserItemsBookingsByStatus(userId, BookingStatus.REJECTED);
        }
    };

    public abstract List<Booking> getUserBookings(BookingRepository repository, Long userId);
    public abstract List<Booking> getUserItemsBookings(BookingRepository repository, Long userId);

    public static BookingsState getByName(String state) {
        final String stateIncludingAllCase = Objects.isNull(state) || Strings.isEmpty(state) ? "ALL" : state;
        Arrays.stream(values()).filter(it -> it.name().equalsIgnoreCase(stateIncludingAllCase)).findAny()
                .orElseThrow(() -> new UnsupportedStatusException(state));
        return BookingsState.valueOf(stateIncludingAllCase);
    }
}
