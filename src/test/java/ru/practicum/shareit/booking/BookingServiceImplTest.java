package ru.practicum.shareit.booking;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.*;
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


import java.lang.reflect.Executable;
import java.time.LocalDateTime;
import java.util.*;
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
    private static final Long BOOKING_ID_1 = 1L;
    private static final Long BOOKING_ID_2 = 2L;
    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long OTHER_ID = 3L;
    private static final Long ITEM_ID = 1L;
    private static final String REQUESTER_OWNER = "_FOR_OWNER";
    private static final String CORRECT_CONDITION_NAME = "WAITING";
    private static final int START_ELEMENT_INDEX = 0;
    private static final int PAGE_SIZE_1 = 1;
    private static final int PAGE_SIZE_20 = 20;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_1 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_1);
    private static final Pageable PAGEABLE_20 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_20);
    private static Page<Booking> PAGE_OF_BOOKINGS_1;
    private static Page<Booking> PAGE_OF_BOOKINGS_20;
    private static final int TOTAL_BOOKINGS_NUMBER = 2;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Spy
    private Map<BookingServiceImpl.SearchCondition, BiFunction<Long, Pageable, Page<Booking>>> conditions = new HashMap<>();

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingInputDto bookingInputDto;
    private User user;
    private User owner;
    private User other;
    private Item item;
    private Booking booking1;
    private Booking booking2;
    private BookingFullDto bookingFullDto1;
    private BookingFullDto bookingFullDto2;

    @Captor
    ArgumentCaptor<Booking> bookingCaptor;

    @BeforeEach
    void beforeEach() {

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

        other = User.builder()
                .id(OTHER_ID)
                .name("otherName")
                .email("otherName@email.ru")
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

        booking1 = Booking.builder()
                .id(BOOKING_ID_1)
                .start(START)
                .end(END)
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        booking2 = Booking.builder()
                .id(BOOKING_ID_2)
                .start(START)
                .end(END)
                .item(item)
                .booker(other)
                .status(BookingStatus.WAITING)
                .build();

        bookingFullDto1 = BookingFullDto.builder()
                .id(BOOKING_ID_1)
                .start(START)
                .end(END)
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();


        bookingFullDto2 = BookingFullDto.builder()
                .id(BOOKING_ID_2)
                .start(START)
                .end(END)
                .item(item)
                .booker(other)
                .status(BookingStatus.WAITING)
                .build();

        PAGE_OF_BOOKINGS_1 = spy(new PageImpl<>(List.of(booking1), PAGEABLE_1, TOTAL_BOOKINGS_NUMBER));
        PAGE_OF_BOOKINGS_20 = spy(new PageImpl<>(List.of(booking1, booking2), PAGEABLE_20, TOTAL_BOOKINGS_NUMBER));
    }

    @Test
    void create_whenUserExistItemAvailableAuthorIsNotOwner_thenReturnBookingFullDto() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking1);

        BookingFullDto actual = bookingService.create(bookingInputDto, USER_ID);
        BookingFullDto expected = bookingFullDto1;

        assertThat(expected, samePropertyValuesAs(actual));
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(userRepository, times(1)).findById(USER_ID);
        verify(itemRepository, times(2)).findById(ITEM_ID);
    }

    @Test
    void create_whenUserNotExistItemAvailableAuthorIsNotOwner_thenThrowNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ShareItElementNotFoundException.class, () -> bookingService.create(bookingInputDto, USER_ID));

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(itemRepository, never()).findById(ITEM_ID);
    }

    @Test
    void create_whenUserExistItemNotExist_thenThrowNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
        assertThrows(ShareItElementNotFoundException.class, () -> bookingService.create(bookingInputDto, USER_ID));

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(userRepository, times(1)).findById(USER_ID);
        verify(itemRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    void create_whenUserExistItemNotAvailableAuthorIsNotOwner_thenThrowNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(itemRepository.findById(bookingInputDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(ItemIsUnavailableException.class, () -> {
            bookingService.create(bookingInputDto, USER_ID);
        });

        verify(userRepository, times(1)).findById(USER_ID);
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
        when(bookingRepository.findById(BOOKING_ID_1)).thenReturn(Optional.of(booking1));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        BookingFullDto actual = bookingService.getById(OWNER_ID, BOOKING_ID_1);
        BookingFullDto expected = bookingFullDto1;

        assertThat(expected, samePropertyValuesAs(actual));
        verify(bookingRepository, times(1)).findById(BOOKING_ID_1);
        verify(itemRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    void getById_whenBookingExistUserIsAuthor_thenReturnBookingFullDto() {
        when(bookingRepository.findById(BOOKING_ID_1)).thenReturn(Optional.of(booking1));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        BookingFullDto actual = bookingService.getById(USER_ID, BOOKING_ID_1);
        BookingFullDto expected = bookingFullDto1;

        assertThat(expected, samePropertyValuesAs(actual));
        verify(bookingRepository, times(2)).findById(BOOKING_ID_1);
        verify(itemRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    void getById_whenBookingNotExistUserIsOwner_thenThrowNotFound() {
        String expectedMessage = "Booking not found.";
        when(bookingRepository.findById(BOOKING_ID_1)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class, () -> bookingService.getById(OWNER_ID, BOOKING_ID_1));

        assertEquals(expectedMessage, actual.getMessage());
        verify(bookingRepository, times(1)).findById(BOOKING_ID_1);
        verify(itemRepository, never()).findById(ITEM_ID);
    }

    @Test
    void getById_whenBookingExistUserIsNeitherOwnerNorAuthor_thenThrowNotFound() {
        when(bookingRepository.findById(BOOKING_ID_1)).thenReturn(Optional.of(booking1));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        assertThrows(ShareItElementNotFoundException.class, () -> bookingService.getById(OTHER_ID, BOOKING_ID_1));

        verify(bookingRepository, times(2)).findById(BOOKING_ID_1);
        verify(itemRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    void findBookings_whenUserNotExistCorrectConditionNameCorrectRequester_thenThrowNotFound() {
        String expectedMessage = "User not found.";
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class, () -> bookingService.findBookings(
                        USER_ID,
                        CORRECT_CONDITION_NAME,
                        REQUESTER_OWNER,
                        START_ELEMENT_INDEX,
                        PAGE_SIZE_1
                ));
        assertEquals(expectedMessage, actual.getMessage());
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(conditions);
    }

    @Test
    void findBookings_whenUserExistConditionNameIncorrect_thenThrowUnsupported() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        String incorrectConditionName = "WAITINGG";

        Exception actual = assertThrows(UnsupportedStatusException.class, () -> bookingService.findBookings(
                USER_ID,
                incorrectConditionName,
                REQUESTER_OWNER,
                START_ELEMENT_INDEX,
                PAGE_SIZE_1
        ));
        assertThat(actual.getMessage(), containsString(incorrectConditionName));
        verify(conditions, times(1)).isEmpty();
        verify(conditions, never()).get(any(BookingServiceImpl.SearchCondition.class));
        verify(PAGE_OF_BOOKINGS_1, never()).getContent();
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void findBookings_whenUserExistConditionNameEmpty_thenReturnBookingFullDtoList() {
        String emptyConditionName = Strings.EMPTY;
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllUserItemsBookings(OWNER_ID, PAGEABLE_20))
                .thenReturn(PAGE_OF_BOOKINGS_20);

        List<BookingFullDto> expected = List.of(bookingFullDto1, bookingFullDto2);
        List<BookingFullDto> actual = bookingService.findBookings(
                OWNER_ID,
                emptyConditionName,
                REQUESTER_OWNER,
                START_ELEMENT_INDEX,
                PAGE_SIZE_20
        );

        assertEqualLists(expected, actual);
        verify(PAGE_OF_BOOKINGS_20, times(1)).getContent();
        verify(userRepository, times(1)).findById(OWNER_ID);
    }

    @Test
    void findBookings_whenUserExistConditionNameCorrect_thenReturnBookingFullDtoList() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(bookingRepository.findUserItemsBookingsWaiting(OWNER_ID, PAGEABLE_1))
                .thenReturn(PAGE_OF_BOOKINGS_1);

        List<BookingFullDto> expected = List.of(bookingFullDto1);
        List<BookingFullDto> actual = bookingService.findBookings(
                OWNER_ID,
                CORRECT_CONDITION_NAME,
                REQUESTER_OWNER,
                START_ELEMENT_INDEX,
                PAGE_SIZE_1
        );

        assertEqualLists(expected, actual);
        verify(PAGE_OF_BOOKINGS_1, times(1)).getContent();
        verify(userRepository, times(1)).findById(OWNER_ID);
    }

    @Test
    void setStatusApproved_whenBookingExistUserIsOwner_thenReturnBookingFullDtoWithApprovedStatus() {
        when(bookingRepository.findById(BOOKING_ID_1)).thenReturn(Optional.of(booking1));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.save(booking1)).thenReturn(booking1);

        BookingFullDto actualDto = bookingService.setStatus(OWNER_ID, BOOKING_ID_1, true);

        booking1.setStatus(BookingStatus.APPROVED);
        BookingFullDto expectedDto = BookingMapper.toBookingFullDto(booking1);
        assertThat(actualDto, samePropertyValuesAs(expectedDto));

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking actual = bookingCaptor.getValue();
        assertThat(actual.getStatus(), is(BookingStatus.APPROVED));

        verify(bookingRepository, times(1)).findById(BOOKING_ID_1);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void setStatusApproved_whenBookingNotExist_thenThrowNotFound() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        String messageExpected = "Booking not found.";

        Exception exception = assertThrows(ShareItElementNotFoundException.class, () -> bookingService.setStatus(OWNER_ID, BOOKING_ID_1, true));

        assertThat(exception.getMessage(), is(messageExpected));
        verify(bookingRepository, times(1)).findById(BOOKING_ID_1);
        verify(itemRepository, never()).findById(ITEM_ID);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void setStatusApproved_whenBookingExistUserIsNotOwner_thenThrowNotFound() {
        when(bookingRepository.findById(BOOKING_ID_1)).thenReturn(Optional.of(booking1));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        String messageExpected = "Item not found.";

        Exception exception = assertThrows(ShareItElementNotFoundException.class, () -> bookingService.setStatus(USER_ID, BOOKING_ID_1, true));

        assertThat(exception.getMessage(), is(messageExpected));
        verify(bookingRepository, times(1)).findById(BOOKING_ID_1);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void setStatusApproved_whenBookingExistUserIsOwnerStatusIsApproved_thenThrowNotFound() {
        booking1.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(BOOKING_ID_1)).thenReturn(Optional.of(booking1));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        String messageExpected = "Booking not found.";

        Exception exception = assertThrows(BookingIsAlreadyApprovedException.class, () -> bookingService.setStatus(OWNER_ID, BOOKING_ID_1, true));

        assertThat(exception.getMessage(), is(messageExpected));
        verify(bookingRepository, times(1)).findById(BOOKING_ID_1);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void setStatusRejected_whenBookingExistUserIsOwner_thenReturnBookingFullDtoWithRejectedStatus() {
        when(bookingRepository.findById(BOOKING_ID_1)).thenReturn(Optional.of(booking1));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.save(booking1)).thenReturn(booking1);

        BookingFullDto actualDto = bookingService.setStatus(OWNER_ID, BOOKING_ID_1, false);

        bookingFullDto1.setStatus(BookingStatus.REJECTED);
        BookingFullDto expectedDto = bookingFullDto1;
        assertThat(actualDto, samePropertyValuesAs(expectedDto));

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking actual = bookingCaptor.getValue();
        assertThat(actual.getStatus(), is(BookingStatus.REJECTED));

        verify(bookingRepository, times(1)).findById(BOOKING_ID_1);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(bookingRepository, times(1)).save(any());
    }
    private static <T> void assertEqualLists(List<T> expected, List<T> actual) {
        assertListSize(expected, actual);
        assertListsContainAll(expected, actual);
    }

    private static <T> void assertListSize(List<T> expected, List<T> actual) {
        assertEquals(expected.size(), actual.size());
    }

    private static <T> void assertListsContainAll(List<T> expected, List<T> actual) {
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }
}