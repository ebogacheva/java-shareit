package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.BookingsState;
import ru.practicum.shareit.exception.AccessForbiddenException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String EXCEPTION_USER_NOT_FOUND_INFO = "User not found.";
    private static final String EXCEPTION_ITEM_NOT_FOUND_INFO = "Item not found.";
    private static final String EXCEPTION_BOOKING_NOT_FOUND_INFO = "Booking not found.";
    private static final String EXCEPTION_CHANGE_STATUS_ACCESS_FORBIDDEN_INFO = "Only owner can approve or reject the booking.";
    private static final String EXCEPTION_BOOKING_ACCESS_FORBIDDEN_INFO = "Booking can be accessed by its owner or an author only.";

    BookingRepository bookingRepository;
    UserRepository userRepository;
    ItemRepository itemRepository;

    @Override
    public BookingDto create(BookingDto bookingDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_USER_NOT_FOUND_INFO));
        Item item = itemRepository.findById(bookingDto.getItem())
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_ITEM_NOT_FOUND_INFO));
        Booking bookingFromDto = BookingMapper.toBooking(bookingDto, user);
        return BookingMapper.toBookingDto(bookingRepository.save(bookingFromDto));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        if (!userIsItemOwner(userId, bookingId) && !userIsBookingAuthor(userId, bookingId)) {
            throw new AccessForbiddenException(EXCEPTION_BOOKING_ACCESS_FORBIDDEN_INFO);
        }
        Booking booking = findById(bookingId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findUserBookings(Long userId, BookingsState state) {
        return BookingMapper.toBookingDtoList(state.getUserBookings(bookingRepository, userId));
    }

    @Override
    public List<BookingDto> findUserItemsBookings(Long userId, BookingsState state) {
        return BookingMapper.toBookingDtoList(state.getUserItemsBookings(bookingRepository, userId));
    }

    @Override
    public BookingDto setStatus(Long userId, Long bookingId, boolean status) {
        Booking booking = findById(bookingId);
        if (!userIsItemOwner(userId, bookingId)) {
            throw new AccessForbiddenException(EXCEPTION_CHANGE_STATUS_ACCESS_FORBIDDEN_INFO);
        };
        booking.setStatus(BookingStatus.getApprovedOrRejected(status));
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    private Booking findById(Long bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        return optionalBooking.orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_BOOKING_NOT_FOUND_INFO));
    }

    private boolean userIsItemOwner(Long userId, Long bookingId) {
        Long ownerId = findById(bookingId).getItem().getOwner().getId();
        return Objects.equals(ownerId, userId);
    }

    private boolean userIsBookingAuthor(Long userId, Long bookingId) {
        Long authorId = findById(bookingId).getBooker().getId();
        return Objects.equals(authorId, userId);
    }

}
