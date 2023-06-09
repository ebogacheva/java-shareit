package ru.practicum.shareit.booking.dto.validation;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingFullDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.util.Objects;

public class BookingStartEndValidator implements ConstraintValidator<BookingStartEndValidation, BookingDto> {
    @Override
    public void initialize(BookingStartEndValidation releaseDate) {
    }

    @Override
    public boolean isValid(BookingDto bookingDto, ConstraintValidatorContext constraintValidatorContext) {
        boolean startIsNotNull = Objects.nonNull(bookingDto.getStart());
        boolean endIsNotNull = Objects.nonNull(bookingDto.getEnd());
        if (startIsNotNull && endIsNotNull) {
            boolean startIsFuture = bookingDto.getStart().isAfter(LocalDateTime.now());
            boolean endIsFuture = bookingDto.getEnd().isAfter(LocalDateTime.now());
            boolean startIsBeforeEnd = bookingDto.getStart().isBefore(bookingDto.getEnd());
            return startIsFuture && endIsFuture && startIsBeforeEnd;
        }
        return false;
    }

}
