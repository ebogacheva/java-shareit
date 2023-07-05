package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemInRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RequestWithItemsDto {

    private Long id;
    private String description;
    private Long requester;
    private LocalDateTime created;
    private List<ItemInRequestDto> items;

}
