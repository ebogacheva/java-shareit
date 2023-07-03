package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    ItemOutDto create(ItemInputDto itemInputDto, Long userId);

    ItemFullDto getById(Long userId, Long itemId);

    List<ItemFullDto> findAll(Long userId, int from, int size);

    ItemOutDto update(ItemInputDto itemInputDto, Long userId, Long itemId);

    List<ItemOutDto> search(String searchBy, int from, int size);

    CommentFullDto addComment(CommentInputDto comment, Long itemId, Long userId);
}
