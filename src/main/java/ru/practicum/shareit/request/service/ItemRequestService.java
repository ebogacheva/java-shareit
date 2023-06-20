package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.dto.RequestWithResponsesDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestFullDto create(ItemRequestDto itemRequestDto, Long userId);

    List<RequestWithResponsesDto> findAll(Long userId);

    List<RequestWithResponsesDto> findAll(Long userId, int from, int size);


}
