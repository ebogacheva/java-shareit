package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentPostRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemDto itemDto, Long userId);

    ItemResponseDto getById(Long userId, Long itemId);

    List<ItemResponseDto> findAll(Long userId, int from, int size);

    ItemDto update(ItemDto itemDto, Long userId, Long itemId);

    List<ItemDto> search(String searchBy, int from, int size);

    CommentResponseDto addComment(CommentPostRequestDto comment, Long itemId, Long userId);
}
