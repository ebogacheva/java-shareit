package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;

public interface ItemRequestService {

    public ItemRequestFullDto create(ItemRequestDto itemRequestDto, Long userId);
}
