package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class ItemInRequestDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;

}
