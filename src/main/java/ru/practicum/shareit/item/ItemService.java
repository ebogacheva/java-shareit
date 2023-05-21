package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemService {

    Item create(ItemDto itemDto, Long userId);
    Item getById(Long itemId);
    List<Item> findAll();
    Item update(ItemDto itemDto, Long itemId);
    void delete(Long itemId);
}
