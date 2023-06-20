package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.BookingInItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class ItemFullDto {

    private Long id;
    private String name;
    private Boolean available;
    private String description;
    private BookingInItemDto lastBooking;
    private BookingInItemDto nextBooking;
    private List<CommentFullDto> comments;
}
