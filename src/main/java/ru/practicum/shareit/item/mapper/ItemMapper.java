package ru.practicum.shareit.item.mapper;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseInRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        Long requestId = Objects.nonNull(item.getRequest()) ? item.getRequest().getId() : null;
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getOwner().getId(),
                requestId
        );
    }

    public static ItemResponseDto toItemResponseDto(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }

    public static void updateItemWithItemDto(Item item, ItemDto itemDto) {
        if (Objects.nonNull(itemDto.getName())) {
            item.setName(itemDto.getName());
        }
        if (Objects.nonNull(itemDto.getDescription())) {
            item.setDescription(itemDto.getDescription());
        }
        if (Objects.nonNull(itemDto.getAvailable())) {
            item.setAvailable(itemDto.getAvailable());
        }
    }

    public static Item toItem(ItemDto itemDto, User user, ItemRequest request) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .request(request)
                .build();
    }

    public static List<ItemDto> toItemDtoList(Page<Item> items) {
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public static ItemResponseInRequestDto toItemResponseInRequest(Item item) {
        return ItemResponseInRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(item.getRequest().getId())
                .build();
    }

    public static List<ItemResponseInRequestDto> toItemResponseInRequestDtoList(List<Item> items) {
        return items.stream().map(ItemMapper::toItemResponseInRequest).collect(Collectors.toList());
    }
    public static List<ItemResponseInRequestDto> toItemResponseInRequestDtoList(Page<Item> items) {
        return items.stream().map(ItemMapper::toItemResponseInRequest).collect(Collectors.toList());
    }

}
