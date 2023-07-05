package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.booking.dto.validation.BookingStartEndValidation;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@BookingStartEndValidation
@EqualsAndHashCode
public class BookingBaseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
}
