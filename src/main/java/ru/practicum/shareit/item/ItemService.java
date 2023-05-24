package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item create(ItemDto itemDto, Long userId);

    Item getById(Long itemId);

    List<Item> findAll(Long userId);

    Item update(ItemDto itemDto, Long userId, Long itemId);

    List<Item> search(String searchBy);
}
