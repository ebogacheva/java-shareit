package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder(toBuilder = true)
public class Item {
    private Long id;
    private String name;
    private String description;
    private boolean available;
    private User owner;
    private ItemRequest request;
}
