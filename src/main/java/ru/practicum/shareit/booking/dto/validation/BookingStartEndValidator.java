package ru.practicum.shareit.booking.dto.validation;

import ru.practicum.shareit.booking.dto.BookingBaseDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.util.Objects;

public class BookingStartEndValidator implements ConstraintValidator<BookingStartEndValidation, BookingBaseDto> {
    @Override
    public void initialize(BookingStartEndValidation releaseDate) {
    }

    @Override
    public boolean isValid(BookingBaseDto bookingBaseDto, ConstraintValidatorContext constraintValidatorContext) {
        boolean startIsNotNull = Objects.nonNull(bookingBaseDto.getStart());
        boolean endIsNotNull = Objects.nonNull(bookingBaseDto.getEnd());
        if (startIsNotNull && endIsNotNull) {
            boolean startIsFuture = bookingBaseDto.getStart().isAfter(LocalDateTime.now());
            boolean endIsFuture = bookingBaseDto.getEnd().isAfter(LocalDateTime.now());
            boolean startIsBeforeEnd = bookingBaseDto.getStart().isBefore(bookingBaseDto.getEnd());
            return startIsFuture && endIsFuture && startIsBeforeEnd;
        }
        return false;
    }

}
