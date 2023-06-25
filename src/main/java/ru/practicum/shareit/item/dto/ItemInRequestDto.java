package ru.practicum.shareit.item.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ItemInRequestDto {

    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;

}
