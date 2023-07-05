package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BookingIsAlreadyApprovedException;
import ru.practicum.shareit.exception.ItemIsUnavailableException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceRepositoryIntegrationTest {

    private static final LocalDateTime START = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime END = LocalDateTime.now().plusWeeks(2);
    private static final int PAGE_SIZE_20 = 20;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_20 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_20);

    private final BookingServiceImpl bookingService;
    private final BookingRepository bookingRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;

    private BookingInputDto bookingInputDto;
    private ItemInputDto itemInputDto;
    private Long userId;
    private Long bookerId;
    private Long itemId;
    private Long bookingId;

    @BeforeEach
    void beforeEach() {
        // default owner - saved to db
        UserDto userInputDto = UserDto.builder()
                .id(null)
                .name("userName")
                .email("user@email.ru")
                .build();
        UserDto userDto = userService.create(userInputDto);
        userId = userDto.getId();

        // default booker - saved to db
        UserDto bookerInputDto = UserDto.builder()
                .id(null)
                .name("bookerName")
                .email("booker@email.ru")
                .build();
        UserDto bookerDto = userService.create(bookerInputDto);
        bookerId = bookerDto.getId();

        // default item - available, saved to db
        itemInputDto = ItemInputDto.builder()
                .id(null)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(userId)
                .requestId(null)
                .build();
        ItemOutDto itemOutDto = itemService.create(itemInputDto, userId);
        itemId = itemOutDto.getId();

        // default bookingInputDto - ready for saving and testing (FUTURE, WAITING)
        bookingInputDto = BookingInputDto.builder()
                .id(null)
                .start(START)
                .end(END)
                .itemId(itemId)
                .status(null)
                .build();
    }

    @Test
    void create_whenValidInput_thenBookingExistInDb() {
        // saving valid input for booking
        BookingFullDto expectedDto = bookingService.create(bookingInputDto, bookerId);
        bookingId = expectedDto.getId();

        // then booking is saved in db
        Optional<Booking> actualBooking = bookingRepository.findById(bookingId);
        assertTrue(actualBooking.isPresent());

        BookingFullDto actualDto = BookingMapper.toBookingFullDto(actualBooking.get());
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void create_whenUserIsItemOwner_thenNoBookingSavedInDb() {
        // creating booking by the owner of the item
        // then throw exception in service
        assertThrows(ShareItElementNotFoundException.class,
                () -> bookingService.create(bookingInputDto, userId));
        // and no bookings saved in db
        List<Booking> actual = bookingRepository.findAllUserBookings(userId, PAGEABLE_20).getContent();
        assertTrue(actual.isEmpty());
    }

    @Test
    void create_whenItemIsNotAvailable_thenNoBookingSavedInDb() {
        //making Item not available
        itemInputDto = ItemInputDto.builder()
                .available(false)
                .build();
        itemService.update(itemInputDto, userId, itemId);

        //then Item could not be booked
        assertThrows(ItemIsUnavailableException.class,
                () -> bookingService.create(bookingInputDto, bookerId));
        List<Booking> actual = bookingRepository.findAllUserBookings(bookerId, PAGEABLE_20).getContent();
        assertTrue(actual.isEmpty());
    }

    @Test
    void getById_whenBookingExist_thenReturnBookingDto() {
        BookingFullDto expectedDto = bookingService.create(bookingInputDto, bookerId);
        bookingId = expectedDto.getId();
        Optional<Booking> actualBooking = bookingRepository.findById(bookingId);
        assertTrue(actualBooking.isPresent());

        BookingFullDto actualDto = BookingMapper.toBookingFullDto(actualBooking.get());
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void getById_whenBookingNotExist_thenReturnEmptyOptional() {
        bookingService.create(bookingInputDto, bookerId);
        //looking for invalid bookingId
        bookingId = 1000L;
        Optional<Booking> actualBooking = bookingRepository.findById(bookingId);

        //then optional is empty
        assertTrue(actualBooking.isEmpty());
    }

    @Test
    void findBookings_whenRequestedCurrentBookingsForBooker_thenReturnListOfBookingsPaged() {
        //make booking current by setting start-date
        bookingInputDto.setStart(LocalDateTime.now().minusWeeks(1));
        BookingFullDto bookingFullDto1 = bookingService.create(bookingInputDto, bookerId);

        //make booking current by setting start-date
        bookingInputDto.setStart(LocalDateTime.now().minusDays(3));
        BookingFullDto bookingFullDto2 = bookingService.create(bookingInputDto, bookerId);

        //make booking future by setting start-date
        bookingInputDto.setStart(LocalDateTime.now().plusDays(1));
        BookingFullDto bookingFullDto3 = bookingService.create(bookingInputDto, bookerId);

        //then return page of current bookings only
        List<BookingFullDto> expected = List.of(bookingFullDto1, bookingFullDto2);
        List<BookingFullDto> actual = BookingMapper.toBookingDtoList(bookingRepository.findCurrentUserBookings(bookerId, PAGEABLE_20));

        assertEqualLists(expected, actual);
    }

    @Test
    void findBookings_whenRequestedRejectedBookingsForOwner_thenReturnListOfBookingsPaged() {
        //saving booking to db
        BookingFullDto bookingFullDto1 = bookingService.create(bookingInputDto, bookerId);
        Long bookingId1 = bookingFullDto1.getId();

        //setting status to REJECTED by the owner, getting saved booking from the db
        bookingFullDto1 = bookingService.setStatus(userId, bookingId1, false);

        //saving other bookings in default state (FUTURE, WAITING)
        bookingService.create(bookingInputDto, bookerId);
        bookingService.create(bookingInputDto, bookerId);

        //then return page of rejected bookings for owner
        List<BookingFullDto> expected = List.of(bookingFullDto1);
        List<BookingFullDto> actual = BookingMapper.toBookingDtoList(
                bookingRepository.findUserItemsBookingsRejected(userId, PAGEABLE_20)
        );

        assertEqualLists(expected, actual);
    }

    @Test
    void setStatus() {
        //saving new booking to db, status is WAITING by default
        BookingFullDto bookingFullDto1 = bookingService.create(bookingInputDto, bookerId);
        Long bookingId1 = bookingFullDto1.getId();

        //setting status to APPROVED
        bookingService.setStatus(userId, bookingId1, true);

        //trying to set status APPROVED again - got BookingIsAlreadyApprovedException
        assertThrows(BookingIsAlreadyApprovedException.class,
                () -> bookingService.setStatus(userId, bookingId1, true));

        //trying to set status REJECTED - got BookingIsAlreadyApprovedException
        assertThrows(BookingIsAlreadyApprovedException.class,
                () -> bookingService.setStatus(userId, bookingId1, false));

        //then no WAITING bookings
        List<BookingFullDto> expected = List.of();
        List<BookingFullDto> actual = BookingMapper.toBookingDtoList(
                bookingRepository.findUserBookingsWaiting(userId, PAGEABLE_20)
        );

        assertEqualLists(expected, actual);
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