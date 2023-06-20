package ru.practicum.shareit.request.model;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestInputDto request, User user) {
        return ItemRequest.builder()
                .description(request.getDescription())
                .requester(user)
                .build();
    }

    public static RequestWithItemsDto toRequestWithItemsDto(ItemRequest request) {
        return RequestWithItemsDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requester(request.getRequester().getId())
                .created(request.getCreated())
                .build();
    }

    public static List<RequestWithItemsDto> toRequestWithItemsDtoList(List<ItemRequest> requests) {
        return requests.stream().map(ItemRequestMapper::toRequestWithItemsDto).collect(Collectors.toList());
    }

    public static List<RequestWithItemsDto> toRequestWithItemsDtoList(Page<ItemRequest> requests) {
        return requests.stream().map(ItemRequestMapper::toRequestWithItemsDto).collect(Collectors.toList());
    }
}
