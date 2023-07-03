package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {

    RequestWithItemsDto create(ItemRequestInputDto itemRequestInputDto, Long userId);

    List<RequestWithItemsDto> findAll(Long userId);

    List<RequestWithItemsDto> findAll(Long userId, int from, int size);

    RequestWithItemsDto getById(Long userId, Long requestId);

}
