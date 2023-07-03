package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    private static final LocalDateTime START = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime END = LocalDateTime.now().plusWeeks(2);
    private static final int PAGE_SIZE_20 = 20;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_20 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_20);

    private Booking booking1;
    private Booking booking2;
    private Booking booking3;
    private Item item;
    private User owner;
    private User booker;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void beforeEach() {
        User ownerInput = User.builder()
                .id(null)
                .name("ownerName")
                .email("owner@email.ru")
                .build();

        User bookerInput = User.builder()
                .id(null)
                .name("bookerName")
                .email("booker@email.ru")
                .build();

        owner = userRepository.save(ownerInput);
        booker = userRepository.save(bookerInput);

        Item itemInput = Item.builder()
                .id(null)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        item = itemRepository.save(itemInput);

        // default state for all bookings: FUTURE, WAITING
        Booking bookingInput1 = Booking.builder()
                .id(null)
                .start(START)
                .end(END)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking bookingInput2 = Booking.builder()
                .id(null)
                .start(START)
                .end(END)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking bookingInput3 = Booking.builder()
                .id(null)
                .start(START)
                .end(END)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        booking1 = bookingRepository.save(bookingInput1);
        booking2 = bookingRepository.save(bookingInput2);
        booking3 = bookingRepository.save(bookingInput3);
    }

    @AfterEach
    public void afterEach() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllUserBookings_thenReturnListOfUsers() {
        List<Booking> actual = bookingRepository.findAllUserBookings(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected = List.of(booking1, booking2, booking3);
        assertEqualLists(expected, actual);
    }

    @Test
    void findCurrentUserBookings_thenReturnListOfBookings() {
        makeBookingCurrent(booking1);
        List<Booking> actual = bookingRepository.findCurrentUserBookings(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected = List.of(booking1);
        assertEqualLists(expected, actual);
    }

    @Test
    void findCurrentUserBookings_thenReturnLEmptyList() {
        List<Booking> actual = bookingRepository.findCurrentUserBookings(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected = List.of();
        assertEqualLists(expected, actual);
    }

    @Test
    void findFutureUserBookings_thenReturnListOfBookings() {
        List<Booking> actual = bookingRepository.findFutureUserBookings(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected = List.of(booking1, booking2, booking3);
        assertEqualLists(expected, actual);
    }

    @Test
    void findPastUserBookings_thenReturnListOfBookings() {
        makeBookingPast(booking1);
        List<Booking> actual = bookingRepository.findPastUserBookings(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected = List.of(booking1);
        assertEqualLists(expected, actual);
    }

    @Test
    void findUserBookingsWaiting_thenReturnListOfBookings() {
        List<Booking> actual1 = bookingRepository.findUserBookingsWaiting(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected1 = List.of(booking1, booking2, booking3);
        assertEqualLists(expected1, actual1);

        changeBookingStatus(booking1, BookingStatus.APPROVED);

        List<Booking> actual2 = bookingRepository.findUserBookingsWaiting(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected2 = List.of(booking2, booking3);
        assertEqualLists(expected2, actual2);
    }

    @Test
    void findUserBookingsRejected_thenReturnListOfBookings() {
        List<Booking> actual1 = bookingRepository.findUserBookingsRejected(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected1 = List.of();
        assertEqualLists(expected1, actual1);

        changeBookingStatus(booking1, BookingStatus.REJECTED);

        List<Booking> actual2 = bookingRepository.findUserBookingsRejected(booker.getId(), PAGEABLE_20).getContent();
        List<Booking> expected2 = List.of(booking1);
        assertEqualLists(expected2, actual2);
    }

    @Test
    void findAllUserItemsBookings_thenReturnListOfBookings() {
        List<Booking> actual = bookingRepository.findAllUserItemsBookings(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected = List.of(booking1, booking2, booking3);
        assertEqualLists(expected, actual);
    }

    @Test
    void findCurrentUserItemsBookings_thenReturnListOfBookings() {
        List<Booking> actual1 = bookingRepository.findCurrentUserItemsBookings(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected1 = List.of();
        assertEqualLists(expected1, actual1);

        makeBookingCurrent(booking1);

        List<Booking> actual2 = bookingRepository.findCurrentUserItemsBookings(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected2 = List.of(booking1);
        assertEqualLists(expected2, actual2);
    }

    @Test
    void findFutureUserItemsBookings_thenReturnListOfBookings() {
        List<Booking> actual = bookingRepository.findFutureUserItemsBookings(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected = List.of(booking1, booking2, booking3);
        assertEqualLists(expected, actual);
    }

    @Test
    void findPastUserItemsBookings_thenReturnListOfBookings() {
        List<Booking> actual1 = bookingRepository.findPastUserItemsBookings(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected1 = List.of();
        assertEqualLists(expected1, actual1);

        makeBookingPast(booking1);

        List<Booking> actual2 = bookingRepository.findPastUserItemsBookings(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected2 = List.of(booking1);
        assertEqualLists(expected2, actual2);
    }

    @Test
    void findUserItemsBookingsWaiting_thenReturnListOfBookings() {
        List<Booking> actual1 = bookingRepository.findUserItemsBookingsWaiting(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected1 = List.of(booking1, booking2, booking3);
        assertEqualLists(expected1, actual1);

        changeBookingStatus(booking1, BookingStatus.APPROVED);

        List<Booking> actual2 = bookingRepository.findUserItemsBookingsWaiting(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected2 = List.of(booking2, booking3);
        assertEqualLists(expected2, actual2);
    }

    @Test
    void findUserItemsBookingsRejected_thenReturnListOfBookings() {
        List<Booking> actual1 = bookingRepository.findUserItemsBookingsRejected(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected1 = List.of();
        assertEqualLists(expected1, actual1);

        changeBookingStatus(booking1, BookingStatus.REJECTED);

        List<Booking> actual2 = bookingRepository.findUserItemsBookingsRejected(owner.getId(), PAGEABLE_20).getContent();
        List<Booking> expected2 = List.of(booking1);
        assertEqualLists(expected2, actual2);
    }

    @Test
    void findFirst1BookingByItemIdAndStatusAndStartBefore_thenReturnBooking() {
        changeBookingStatus(booking1, BookingStatus.APPROVED);
        makeBookingPast(booking1);
        Sort sortLast = Sort.by("start").descending();
        Optional<Booking> actual = bookingRepository.findFirst1BookingByItemIdAndStatusAndStartBefore(
                        item.getId(), BookingStatus.APPROVED, LocalDateTime.now(), sortLast
        );
        Booking expected = booking1;
        actual.ifPresent(booking -> assertEquals(expected, booking));
    }

    @Test
    void findFirst1BookingByItemIdAndStatusAndStartAfter_thenReturnBooking() {
        changeBookingStatus(booking1, BookingStatus.APPROVED);
        booking2.setStart(booking1.getStart().plusDays(1));
        changeBookingStatus(booking2, BookingStatus.APPROVED);
        Sort sortNext = Sort.by("start").ascending();
        Optional<Booking> actual = bookingRepository.findFirst1BookingByItemIdAndStatusAndStartBefore(
                item.getId(), BookingStatus.APPROVED, LocalDateTime.now(), sortNext
        );
        Booking expected = booking2;
        actual.ifPresent(booking -> assertEquals(expected, booking));
    }

    @Test
    void findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore_thenReturnBooking() {
        changeBookingStatus(booking1, BookingStatus.APPROVED);
        makeBookingPast(booking1);
        Optional<Booking> actual = bookingRepository.findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore(
                booker.getId(), item.getId(), BookingStatus.APPROVED, LocalDateTime.now()
        );
        Booking expected = booking1;
        actual.ifPresent(booking -> assertEquals(expected, booking));
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


    private void makeBookingCurrent(Booking booking) {
        booking.setStart(LocalDateTime.now().minusDays(5));
        booking.setEnd(LocalDateTime.now().plusDays(5));
        bookingRepository.save(booking);
    }

    private void makeBookingPast(Booking booking) {
        booking.setStart(LocalDateTime.now().minusDays(10));
        booking.setEnd(LocalDateTime.now().minusDays(5));
        bookingRepository.save(booking);
    }

    private void changeBookingStatus(Booking booking, BookingStatus status) {
        booking.setStatus(status);
        bookingRepository.save(booking);
    }

}