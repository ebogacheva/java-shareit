package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInItemDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private static final Long BOOKING_ID = 1L;
    private static final LocalDateTime START = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime END = LocalDateTime.now().plusWeeks(2);
    private static final Long BOOKER_ID = 1L;
    private static Long ITEM_ID = 1L;

    private static Booking booking;
    private static BookingFullDto bookingFullDto;
    private static BookingInItemDto bookingInItemDto;
    private static BookingInputDto bookingInputDto;
    private static User booker;
    private static Item item = new Item();

    @BeforeAll
    static void beforeAll() {
        booker = User.builder()
                .id(BOOKER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();

        booking = Booking.builder()
                .id(BOOKING_ID)
                .start(START)
                .end(END)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingFullDto = BookingFullDto.builder()
                .id(BOOKING_ID)
                .start(START)
                .end(END)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingInItemDto = BookingInItemDto.builder()
                .id(BOOKING_ID)
                .start(START)
                .end(END)
                .bookerId(BOOKER_ID)
                .build();

        bookingInputDto = BookingInputDto.builder()
                .id(BOOKING_ID)
                .start(START)
                .end(END)
                .itemId(ITEM_ID)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void toBookingFullDto() {
        BookingFullDto expected = bookingFullDto;
        BookingFullDto actual = BookingMapper.toBookingFullDto(booking);
        assertEquals(expected, actual);
    }

    @Test
    void toBookingInItemDto() {
        BookingInItemDto expected = bookingInItemDto;
        BookingInItemDto actual = BookingMapper.toBookingInItemDto(booking);
        assertEquals(expected, actual);
    }

    @Test
    void toBooking() {
        Booking expected = booking;
        Booking actual = BookingMapper.toBooking(bookingInputDto, item, booker);
        assertEquals(expected, actual);
    }

    @Test
    void toBookingDtoList() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Booking> pageOfBookings = new PageImpl<>(List.of(booking), pageable, 1);
        List<BookingFullDto> expected = List.of(bookingFullDto);
        List<BookingFullDto> actual = BookingMapper.toBookingDtoList(pageOfBookings);
        assertEquals(expected, actual);
    }
}