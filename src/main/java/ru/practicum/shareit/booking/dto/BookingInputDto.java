package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.booking.model.BookingStatus;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class BookingInputDto extends BookingDto{
    private Long itemId;
    private BookingStatus status;
}