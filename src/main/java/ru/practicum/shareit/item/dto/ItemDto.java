package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@JsonDeserialize(using = ItemDtoDeserializer.class)
public class ItemDto {
    private String name;
    private String description;
    private String available;
}
