package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BookingIsAlreadyApprovedException;
import ru.practicum.shareit.exception.ItemIsUnavailableException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
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
    private static final String REQUESTER_OWNER = "_FOR_OWNER";
    private static final String CORRECT_CONDITION_NAME = "WAITING";
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);
    private static Page<Booking> PAGES;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private Map<BookingServiceImpl.SearchCondition, BiFunction<Long, Pageable, Page<Booking>>> conditions = new HashMap<>();
    private BookingService bookingService;
    private BookingInputDto bookingInputDto;
    private User user;
    private User owner;
    private Item item;
    private Booking booking;

    @Captor
    ArgumentCaptor<Booking> bookingCaptor;

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
        PAGES = new PageImpl<>(List.of(booking), PAGEABLE, 1);
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
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

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
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

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
    void findBookings_whenUserNotExistCorrectConditionNameCorrectRequester_thenThrowNotFound() {
        String expectedExceptionMessage = "User not found.";
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ShareItElementNotFoundException.class,
                () -> bookingService.findBookings(USER_ID, CORRECT_CONDITION_NAME, REQUESTER_OWNER, 0, 20));
        assertEquals(expectedExceptionMessage, exception.getMessage());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void findBookings_whenUserExistConditionNameIncorrect_thenThrowUnsupported() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        String incorrectConditionName = "WAITINGG";

        Exception exception = assertThrows(UnsupportedStatusException.class,
                () -> bookingService.findBookings(USER_ID, incorrectConditionName, REQUESTER_OWNER, 0, 20));
        assertThat(exception.getMessage(), containsString(incorrectConditionName));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void findBookings_whenUserExistConditionNameEmpty_thenReturnBookingFullDtoList() {
        String emptyConditionName = "";
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllUserItemsBookings(OWNER_ID, PAGEABLE))
                .thenReturn(PAGES);
        List<BookingFullDto> expected = BookingMapper.toBookingDtoList(PAGES);
        List<BookingFullDto> actual = bookingService.findBookings(OWNER_ID, emptyConditionName, REQUESTER_OWNER, 0, 20);

        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected));
        verify(userRepository, times(1)).findById(OWNER_ID);
    }

    @Test
    void findBookings_whenUserExistConditionNameCorrect_thenReturnBookingFullDtoList() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(bookingRepository.findUserItemsBookingsWaiting(OWNER_ID, PAGEABLE))
                .thenReturn(PAGES);

        List<BookingFullDto> expected = BookingMapper.toBookingDtoList(PAGES);
        List<BookingFullDto> actual = bookingService.findBookings(OWNER_ID, CORRECT_CONDITION_NAME, REQUESTER_OWNER, 0, 20);

        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected));
        verify(userRepository, times(1)).findById(OWNER_ID);
    }

    @Test
    void setStatusApproved_whenBookingExistUserIsOwner_thenReturnBookingFullDtoWithApprovedStatus() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingFullDto actualDto = bookingService.setStatus(OWNER_ID,BOOKING_ID, true);

        booking.setStatus(BookingStatus.APPROVED);
        BookingFullDto expectedDto = BookingMapper.toBookingFullDto(booking);
        assertThat(actualDto, samePropertyValuesAs(expectedDto));

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking actual = bookingCaptor.getValue();
        assertThat(actual.getStatus(), is(BookingStatus.APPROVED));

        verify(bookingRepository, times(1)).findById(BOOKING_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void setStatusApproved_whenBookingNotExist_thenThrowNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        String messageExpected = "Booking not found.";

        Exception exception = assertThrows(ShareItElementNotFoundException.class, () -> bookingService.setStatus(OWNER_ID, BOOKING_ID, true));

        assertThat(exception.getMessage(), is(messageExpected));
        verify(bookingRepository, times(1)).findById(BOOKING_ID);
        verify(itemRepository, never()).findById(ITEM_ID);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void setStatusApproved_whenBookingExistUserIsNotOwner_thenThrowNotFound() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        String messageExpected = "Item not found.";

        Exception exception = assertThrows(ShareItElementNotFoundException.class,
                () -> bookingService.setStatus(USER_ID, BOOKING_ID, true));

        assertThat(exception.getMessage(), is(messageExpected));
        verify(bookingRepository, times(1)).findById(BOOKING_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void setStatusApproved_whenBookingExistUserIsOwnerStatusIsApproved_thenThrowNotFound() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        String messageExpected = "Booking not found.";

        Exception exception = assertThrows(BookingIsAlreadyApprovedException.class,
                () -> bookingService.setStatus(OWNER_ID, BOOKING_ID, true));

        assertThat(exception.getMessage(), is(messageExpected));
        verify(bookingRepository, times(1)).findById(BOOKING_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void setStatusRejected_whenBookingExistUserIsOwner_thenReturnBookingFullDtoWithRejectedStatus() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingFullDto actualDto = bookingService.setStatus(OWNER_ID,BOOKING_ID, false);

        booking.setStatus(BookingStatus.REJECTED);
        BookingFullDto expectedDto = BookingMapper.toBookingFullDto(booking);
        assertThat(actualDto, samePropertyValuesAs(expectedDto));

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking actual = bookingCaptor.getValue();
        assertThat(actual.getStatus(), is(BookingStatus.REJECTED));

        verify(bookingRepository, times(1)).findById(BOOKING_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(bookingRepository, times(1)).save(any());
    }
}