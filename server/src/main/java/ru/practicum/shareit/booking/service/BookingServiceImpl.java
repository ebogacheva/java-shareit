package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingIsAlreadyApprovedException;
import ru.practicum.shareit.exception.ItemIsUnavailableException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String EXCEPTION_USER_NOT_FOUND_INFO = "User not found.";
    private static final String EXCEPTION_ITEM_NOT_FOUND_INFO = "Item not found.";
    private static final String EXCEPTION_BOOKING_NOT_FOUND_INFO = "Booking not found.";
    private static final String EXCEPTION_ITEM_UNAVAILABLE = "Item is unavailable and can't be booked.";

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private Map<SearchCondition, BiFunction<Long, Pageable, Page<Booking>>> conditions;

    @Override
    @Transactional
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
    public List<BookingFullDto> findBookings(Long userId, String conditionName, String requester, int from, int size) {
        getUserIfExists(userId);
        composeConditionsMapIfEmpty();
        BiFunction<Long, Pageable, Page<Booking>> repositoryMethod = getRepositoryMethod(conditionName, requester);
        Page<Booking> bookings = repositoryMethod.apply(userId, pageRequestOf(from, size));
        return BookingMapper.toBookingDtoList(bookings);
    }

    @Override
    @Transactional
    public BookingFullDto setStatus(Long userId, Long bookingId, boolean status) {
        Booking booking = getBookingIfExists(bookingId);
        Long itemId = booking.getItem().getId();
        if (!userIsItemOwner(userId, itemId)) {
            throw new ShareItElementNotFoundException(EXCEPTION_ITEM_NOT_FOUND_INFO);
        }
        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new BookingIsAlreadyApprovedException(EXCEPTION_BOOKING_NOT_FOUND_INFO);
        }
        booking.setStatus(BookingStatus.getApprovedOrRejected(status));
        return BookingMapper.toBookingFullDto(bookingRepository.save(booking));
    }

    private SearchCondition getFullSearchCondition(String conditionName, String requester) {
        final String fullCondition = (conditionName + requester).toUpperCase();
        return Arrays.stream(SearchCondition.values())
                .filter(c -> c.name().equals(fullCondition))
                .findFirst()
                .orElseThrow(() -> new UnsupportedStatusException(conditionName));
    }

    private BiFunction<Long, Pageable, Page<Booking>> getRepositoryMethod(String conditionName, String requester) {
        SearchCondition fullSearchCondition = getFullSearchCondition(conditionName, requester);
        return conditions.get(fullSearchCondition);
    }

    public enum SearchCondition {
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
        REJECTED_FOR_OWNER
    }

    private void composeConditionsMapIfEmpty() {
        if (conditions.isEmpty()) {
            conditions = Map.ofEntries(
                    Map.entry(SearchCondition.ALL_FOR_BOOKER, bookingRepository::findAllUserBookings),
                    Map.entry(SearchCondition.CURRENT_FOR_BOOKER, bookingRepository::findCurrentUserBookings),
                    Map.entry(SearchCondition.PAST_FOR_BOOKER, bookingRepository::findPastUserBookings),
                    Map.entry(SearchCondition.FUTURE_FOR_BOOKER, bookingRepository::findFutureUserBookings),
                    Map.entry(SearchCondition.WAITING_FOR_BOOKER, bookingRepository::findUserBookingsWaiting),
                    Map.entry(SearchCondition.REJECTED_FOR_BOOKER, bookingRepository::findUserBookingsRejected),
                    Map.entry(SearchCondition.ALL_FOR_OWNER, bookingRepository::findAllUserItemsBookings),
                    Map.entry(SearchCondition.CURRENT_FOR_OWNER, bookingRepository::findCurrentUserItemsBookings),
                    Map.entry(SearchCondition.PAST_FOR_OWNER, bookingRepository::findPastUserItemsBookings),
                    Map.entry(SearchCondition.FUTURE_FOR_OWNER, bookingRepository::findFutureUserItemsBookings),
                    Map.entry(SearchCondition.WAITING_FOR_OWNER, bookingRepository::findUserItemsBookingsWaiting),
                    Map.entry(SearchCondition.REJECTED_FOR_OWNER, bookingRepository::findUserItemsBookingsRejected)
            );
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

    private static Pageable pageRequestOf(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
