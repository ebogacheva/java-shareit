package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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

import java.util.*;
import java.util.function.Function;

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
    private final Map<SearchCondition, Function<Long, List<Booking>>> conditions = new HashMap<>();

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
    public List<BookingFullDto> findBookings(Long userId, String conditionName, String requester) {
        getUserIfExists(userId);
        composeConditionsMapIfEmpty();
        Function<Long, List<Booking>> repositoryMethod = getRepositoryMethod(conditionName, requester);
        List<Booking> bookings = repositoryMethod.apply(userId);
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

    private SearchCondition getFullSearchCondition(String conditionName, String requester) {
        final String conditionIncludingAllCase =
                Objects.isNull(conditionName) || Strings.isEmpty(conditionName) ? "ALL" : conditionName;
        final String fullCondition = (conditionIncludingAllCase + requester).toUpperCase();
        return Arrays.stream(SearchCondition.values()).filter(c -> c.name().equals(fullCondition)).findFirst()
                .orElseThrow(() -> new UnsupportedStatusException(conditionName));
    }

    private Function<Long, List<Booking>> getRepositoryMethod(String conditionName, String requester) {
        SearchCondition fullSearchCondition = getFullSearchCondition(conditionName, requester);
        return conditions.get(fullSearchCondition);
    }

    private enum SearchCondition {
        ALL_FOR_BOOKER,
        CURRENT_FOR_BOOKER,
        PAST_FOR_BOOKER,
        FUTURE_FOR_BOOKER,
        WAITING_FOR_BOOKER,
        REJECTED_FOR_BOOKER,
        ALL_FOR_OWNER,
        CURRENT_FOR_OWNER,
        PAST_FOR_OWNER,
        FUTURE_FOR_OWNER,
        WAITING_FOR_OWNER,
        REJECTED_FOR_OWNER;
    }

    private void composeConditionsMapIfEmpty() {
        if (conditions.isEmpty()) {
            conditions.put(SearchCondition.ALL_FOR_BOOKER, bookingRepository::findAllUserBookings);
            conditions.put(SearchCondition.CURRENT_FOR_BOOKER, bookingRepository::findCurrentUserBookings);
            conditions.put(SearchCondition.PAST_FOR_BOOKER, bookingRepository::findPastUserBookings);
            conditions.put(SearchCondition.FUTURE_FOR_BOOKER, bookingRepository::findFutureUserBookings);
            conditions.put(SearchCondition.WAITING_FOR_BOOKER, bookingRepository::findUserBookingsWaiting);
            conditions.put(SearchCondition.REJECTED_FOR_BOOKER, bookingRepository::findUserBookingsRejected);
            conditions.put(SearchCondition.ALL_FOR_OWNER, bookingRepository::findAllUserItemsBookings);
            conditions.put(SearchCondition.CURRENT_FOR_OWNER, bookingRepository::findCurrentUserItemsBookings);
            conditions.put(SearchCondition.PAST_FOR_OWNER, bookingRepository::findPastUserItemsBookings);
            conditions.put(SearchCondition.FUTURE_FOR_OWNER, bookingRepository::findFutureUserItemsBookings);
            conditions.put(SearchCondition.WAITING_FOR_OWNER, bookingRepository::findUserItemsBookingsWaiting);
            conditions.put(SearchCondition.REJECTED_FOR_OWNER, bookingRepository::findUserItemsBookingsRejected);
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
