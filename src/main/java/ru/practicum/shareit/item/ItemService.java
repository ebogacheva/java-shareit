package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemDto itemDto, Long userId);

    ItemResponseDto getById(Long userId, Long itemId);

    List<ItemResponseDto> findAll(Long userId);

    ItemDto update(ItemDto itemDto, Long userId, Long itemId);

    List<ItemDto> search(String searchBy);
}
