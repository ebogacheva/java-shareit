package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ItemIsUnavailableException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final LocalDateTime START = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime END = LocalDateTime.now().plusWeeks(2);
    private static final Long BOOKING_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long OTHER_ID = 3L;
    private static final Long ITEM_ID = 1L;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private Map<BookingServiceImpl.SearchCondition, BiFunction<Long, Pageable, Page<Booking>>> conditions;
    private BookingService bookingService;
    private BookingInputDto bookingInputDto;
    private User user;
    private User owner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void beforeEach() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository, conditions);
        bookingInputDto = BookingInputDto.builder()
                .id(null)
                .start(START)
                .end(END)
                .itemId(1L)
                .status(null)
                .build();

        user = User.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();

        owner = User.builder()
                .id(OWNER_ID)
                .name("ownerName")
                .email("owner@email.ru")
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        booking = Booking.builder()
                .id(BOOKING_ID)
                .start(START)
                .end(END)
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void create_whenUserExistItemAvailableAuthorIsNotOwner_thenReturnBookingFullDto() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingFullDto actual = bookingService.create(bookingInputDto, user.getId());
        BookingFullDto expected = BookingMapper.toBookingFullDto(booking);

        assertThat(expected, samePropertyValuesAs(actual));
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(userRepository, times(1)).findById(USER_ID);
        verify(itemRepository, times(2)).findById(ITEM_ID);
    }

    @Test
    void create_whenUserNotExistItemAvailableAuthorIsNotOwner_thenThrowNotFound() {
        doThrow(new ShareItElementNotFoundException("User not found.")).when(userRepository).findById(user.getId());

        assertThrows(ShareItElementNotFoundException.class, () -> bookingService.create(bookingInputDto, user.getId()));

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(itemRepository, never()).findById(item.getId());
    }

    @Test
    void create_whenUserExistItemNotAvailableAuthorIsNotOwner_thenThrowNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(bookingInputDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(ItemIsUnavailableException.class, () -> bookingService.create(bookingInputDto, user.getId()));

        verify(userRepository, times(1)).findById(user.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_whenUserExistItemIsAvailableAuthorIsOwner_thenThrowNotFound() {
        item.setOwner(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemRepository.findById(bookingInputDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(ShareItElementNotFoundException.class, () -> bookingService.create(bookingInputDto, USER_ID));

        verify(userRepository, times(1)).findById(USER_ID);
        verify(itemRepository, times(2)).findById(ITEM_ID);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getById_whenBookingExistUserIsOwner_thenReturnBookingFullDto() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        BookingFullDto actual = bookingService.getById(OWNER_ID, BOOKING_ID);
        BookingFullDto expected = BookingMapper.toBookingFullDto(booking);

        assertThat(expected, samePropertyValuesAs(actual));
        verify(bookingRepository, times(1)).findById(BOOKING_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    void getById_whenBookingExistUserIsAuthor_thenReturnBookingFullDto() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        BookingFullDto actual = bookingService.getById(USER_ID, BOOKING_ID);
        BookingFullDto expected = BookingMapper.toBookingFullDto(booking);

        assertThat(expected, samePropertyValuesAs(actual));
        verify(bookingRepository, times(2)).findById(BOOKING_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    void getById_whenBookingNotExistUserIsOwner_thenThrowNotFound() {
        String exceptionMessage = "Booking not found.";
        doThrow(new ShareItElementNotFoundException(exceptionMessage)).when(bookingRepository).findById(BOOKING_ID);

        Exception exception = assertThrows(ShareItElementNotFoundException.class, () -> bookingService.getById(OWNER_ID, BOOKING_ID));

        assertEquals(exceptionMessage, exception.getMessage());
        verify(bookingRepository, times(1)).findById(BOOKING_ID);
        verify(itemRepository, never()).findById(ITEM_ID);
    }

    @Test
    void getById_whenBookingExistUserIsNeitherOwnerNorAuthor_thenThrowNotFound() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        assertThrows(ShareItElementNotFoundException.class, () -> bookingService.getById(OTHER_ID, BOOKING_ID));

        verify(bookingRepository, times(2)).findById(BOOKING_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
    }


    @Test
    void findBookings() {
    }

    @Test
    void setStatus() {
    }
}