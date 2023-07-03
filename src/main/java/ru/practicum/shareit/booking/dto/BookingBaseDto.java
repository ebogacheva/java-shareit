package ru.practicum.shareit.booking.dto;

import lombok.*;
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
