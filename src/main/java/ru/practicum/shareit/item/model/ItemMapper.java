package ru.practicum.shareit.item.model;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                String.valueOf(item.isAvailable())
        );
    }

    public static void updateItemWithItemDto(Item item, ItemDto itemDto) {
        if (Objects.nonNull(itemDto.getName())) {
            item.setName(itemDto.getName());
        }
        if (Objects.nonNull(itemDto.getDescription())) {
            item.setDescription(itemDto.getDescription());
        }
        if (Objects.nonNull(itemDto.getAvailable())) {
            item.setAvailable(Boolean.parseBoolean(itemDto.getAvailable()));
        }
    }

    public static Item toItem(ItemDto itemDto, Long itemId, Long userId) {
        return Item.builder()
                .id(itemId)
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(Boolean.parseBoolean(itemDto.getAvailable()))
                .owner(userId)
                .build();
    }

    public static List<ItemDto> toItemDtoList(List<Item> items) {
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

}
