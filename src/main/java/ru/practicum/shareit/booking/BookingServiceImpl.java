package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BookingIsAlreadyApprovedException;
import ru.practicum.shareit.exception.ItemIsUnavailableException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String EXCEPTION_USER_NOT_FOUND_INFO = "User not found.";
    private static final String EXCEPTION_ITEM_NOT_FOUND_INFO = "Item not found.";
    private static final String EXCEPTION_BOOKING_NOT_FOUND_INFO = "Booking not found.";
    private static final String EXCEPTION_ITEM_UNAVAILABLE = "Item is unavailable and can't be booked.";

    private BookingRepository bookingRepository;
    private UserRepository userRepository;
    private ItemRepository itemRepository;

    @Override
    public BookingFullDto create(BookingInputDto bookingInputDto, Long userId) {
        User user = getUserIfExists(userId);
        Item item = getItemIfExists(bookingInputDto.getItemId());
        if (!item.isAvailable()) {
            throw new ItemIsUnavailableException(EXCEPTION_ITEM_UNAVAILABLE);
        }
        if (userIsItemOwner(userId, bookingInputDto.getItemId())) {
            throw new ShareItElementNotFoundException(EXCEPTION_ITEM_NOT_FOUND_INFO);
        }
        bookingInputDto.setStatus(BookingStatus.WAITING);
        Booking bookingFromDto = BookingMapper.toBooking(bookingInputDto, item, user);
        return BookingMapper.toBookingFullDto(bookingRepository.save(bookingFromDto));
    }

    @Override
    public BookingFullDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingIfExists(bookingId);
        Long itemId = booking.getItem().getId();
        if (!userIsItemOwner(userId, itemId) && !userIsBookingAuthor(userId, bookingId)) {
            throw new ShareItElementNotFoundException(EXCEPTION_BOOKING_NOT_FOUND_INFO);
        }
        return BookingMapper.toBookingFullDto(booking);
    }

    @Override
    public List<BookingFullDto> findUserBookings(Long userId, String conditionName) {
        SearchCondition searchCondition = SearchCondition.getByName(conditionName);
        getUserIfExists(userId);
        List<Booking> bookings = searchCondition.getUserBookings(bookingRepository, userId);
        return BookingMapper.toBookingDtoList(bookings);
    }

    @Override
    public List<BookingFullDto> findUserItemsBookings(Long userId, String searchConditionName) {
        SearchCondition searchCondition = SearchCondition.getByName(searchConditionName);
        getUserIfExists(userId);
        List<Booking> bookings = searchCondition.getUserItemsBookings(bookingRepository, userId);
        return BookingMapper.toBookingDtoList(bookings);
    }

    @Override
    public BookingFullDto setStatus(Long userId, Long bookingId, boolean status) {
        Booking booking = getBookingIfExists(bookingId);
        Long itemId = booking.getItem().getId();
        if (!userIsItemOwner(userId, itemId)) {
            throw new ShareItElementNotFoundException(EXCEPTION_ITEM_NOT_FOUND_INFO);
        }
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new BookingIsAlreadyApprovedException(EXCEPTION_BOOKING_NOT_FOUND_INFO);
        }
        booking.setStatus(BookingStatus.getApprovedOrRejected(status));
        return BookingMapper.toBookingFullDto(bookingRepository.save(booking));
    }

    private enum SearchCondition {
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

        public static SearchCondition getByName(String searchCondition) {
            final String conditionIncludingAllCase =
                    Objects.isNull(searchCondition) || Strings.isEmpty(searchCondition) ? "ALL" : searchCondition;
            return Arrays.stream(values()).filter(i -> i.name().equalsIgnoreCase(conditionIncludingAllCase)).findFirst()
                    .orElseThrow(() -> new UnsupportedStatusException(searchCondition));
        }
    }

    private Booking getBookingIfExists(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_BOOKING_NOT_FOUND_INFO));
    }

    private Item getItemIfExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_ITEM_NOT_FOUND_INFO));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_USER_NOT_FOUND_INFO));
    }

    private boolean userIsItemOwner(Long userId, Long itemId) {
        Long ownerId = getItemIfExists(itemId).getOwner().getId();
        return Objects.equals(ownerId, userId);
    }

    private boolean userIsBookingAuthor(Long userId, Long bookingId) {
        Long authorId = getBookingIfExists(bookingId).getBooker().getId();
        return Objects.equals(authorId, userId);
    }
}
