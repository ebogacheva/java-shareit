package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> getById(Long itemId);

    List<Item> findAll();

    Item create(ItemDto itemDto, User user);

    Optional<Item> update(ItemDto itemDto, Long itemId);
    void delete(Long itemId);
}
