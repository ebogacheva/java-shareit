package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInItemDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    public static BookingFullDto toBookingFullDto(Booking booking) {
        return BookingFullDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .booker(booking.getBooker())
                .item(booking.getItem())
                .status(booking.getStatus()).build();
    }

    public static BookingInItemDto toBookingInItemDto(Booking booking) {
        return BookingInItemDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    public static Booking toBooking(BookingFullDto bookingDto, Item item, User booker) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .booker(booker)
                .item(item)
                .status(bookingDto.getStatus())
                .build();
    }

    public static Booking toBooking(BookingInputDto bookingInputDto, Item item, User booker) {
        return Booking.builder()
                .id(bookingInputDto.getId())
                .start(bookingInputDto.getStart())
                .end(bookingInputDto.getEnd())
                .booker(booker)
                .item(item)
                .status(bookingInputDto.getStatus())
                .build();
    }

    public static List<BookingFullDto> toBookingDtoList(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toBookingFullDto).collect(Collectors.toList());
    }
}
