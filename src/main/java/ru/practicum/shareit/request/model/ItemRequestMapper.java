package ru.practicum.shareit.request.model;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.user.model.User;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestDto request, User user) {
        return ItemRequest.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestor(user)
                .build();
    }

    public static ItemRequestFullDto toItemRequestFullDto(ItemRequest request) {
        return ItemRequestFullDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requester(request.getRequestor().getId())
                .created(request.getCreated())
                .build();
    }
}
