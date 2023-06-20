package ru.practicum.shareit.request.model;

import org.springframework.data.domain.Page;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.dto.RequestWithResponsesDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestDto request, User user) {
        return ItemRequest.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requester(user)
                .build();
    }

    public static ItemRequestFullDto toItemRequestFullDto(ItemRequest request) {
        return ItemRequestFullDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requester(request.getRequester().getId())
                .created(request.getCreated())
                .build();
    }

    public static RequestWithResponsesDto toRequestWithResponsesDto(ItemRequest request) {
        return RequestWithResponsesDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requester(request.getRequester().getId())
                .created(request.getCreated())
                .build();
    }

    public static List<RequestWithResponsesDto> toRequestWithResponsesDtoList(List<ItemRequest> requestItems) {
        return requestItems.stream().map(ItemRequestMapper::toRequestWithResponsesDto).collect(Collectors.toList());
    }

    public static List<RequestWithResponsesDto> toRequestWithResponsesDtoList(Page<ItemRequest> requests) {
        return requests.stream().map(ItemRequestMapper::toRequestWithResponsesDto).collect(Collectors.toList());
    }
}
