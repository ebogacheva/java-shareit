package ru.practicum.shareit.item.model;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import java.util.Objects;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getName(),
                item.getDescription(),
                String.valueOf(item.isAvailable())
        );
    }

    public static void updateItemWithItemDto(Item item, ItemDto itemDto) {
        if (Objects.nonNull(itemDto.getName())) {
            item.setName(item.getName());
        }
        if (Objects.nonNull(itemDto.getDescription())) {
            item.setDescription(item.getDescription());
        }
        if (Objects.nonNull(itemDto.getAvailable())) {
            item.setAvailable(Boolean.getBoolean(itemDto.getAvailable()));
        }
    }

    public static Item toItem(ItemDto itemDto, Long itemId, User user) {
        return Item.builder()
                .id(itemId)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(Boolean.getBoolean(itemDto.getAvailable()))
                .owner(user)
                .build();
    }

}
