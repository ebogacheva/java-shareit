package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemFullDto;

import java.util.List;

public interface ItemService {

    ItemInputDto create(ItemInputDto itemInputDto, Long userId);

    ItemFullDto getById(Long userId, Long itemId);

    List<ItemFullDto> findAll(Long userId, int from, int size);

    ItemInputDto update(ItemInputDto itemInputDto, Long userId, Long itemId);

    List<ItemInputDto> search(String searchBy, int from, int size);

    CommentFullDto addComment(CommentInputDto comment, Long itemId, Long userId);
}
