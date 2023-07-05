package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.dto.ItemInRequestDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.model.Item;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemOutDto toItemOutDto(Item item) {
        Long requestId = Objects.nonNull(item.getRequest()) ? item.getRequest().getId() : null;
        return new ItemOutDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getOwner().getId(),
                requestId
        );
    }

    public static ItemFullDto toItemFullDto(Item item) {
        return ItemFullDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }

    public static void updateItemWithItemDto(Item item, ItemInputDto itemInputDto) {
        if (Objects.nonNull(itemInputDto.getName())) {
            item.setName(itemInputDto.getName());
        }
        if (Objects.nonNull(itemInputDto.getDescription())) {
            item.setDescription(itemInputDto.getDescription());
        }
        if (Objects.nonNull(itemInputDto.getAvailable())) {
            item.setAvailable(itemInputDto.getAvailable());
        }
    }

    public static Item toItem(ItemInputDto itemInputDto, User user, ItemRequest request) {
        return Item.builder()
                .id(itemInputDto.getId())
                .name(itemInputDto.getName())
                .description(itemInputDto.getDescription())
                .available(itemInputDto.getAvailable())
                .owner(user)
                .request(request)
                .build();
    }

    public static List<ItemOutDto> toItemDtoList(Page<Item> pageOfItems) {
        return pageOfItems.getContent().stream().map(ItemMapper::toItemOutDto).collect(Collectors.toList());
    }

    public static ItemInRequestDto toItemResponseInRequest(Item item) {
        return ItemInRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .requestId(item.getRequest().getId())
                .build();
    }

    public static List<ItemInRequestDto> toItemResponseInRequestDtoList(List<Item> items) {
        return items.stream().map(ItemMapper::toItemResponseInRequest).collect(Collectors.toList());
    }
}
