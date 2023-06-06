package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto getById(Long itemId);

    List<ItemDto> findAll(Long userId);

    ItemDto update(ItemDto itemDto, Long userId, Long itemId);

    List<Item> search(String searchBy);
}
