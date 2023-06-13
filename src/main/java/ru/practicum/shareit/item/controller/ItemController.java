package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentPostRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                       @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(itemDto, userId);
    }

    @GetMapping(value = "/{itemId}")
    public ItemResponseDto getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                           @PathVariable Long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemResponseDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        return itemService.findAll(userId);
    }

    @PatchMapping(value = "/{itemId}")
    public ItemDto update(@RequestHeader(X_SHARER_USER_ID) long userId,
                       @PathVariable Long itemId,
                       @RequestBody ItemDto itemDto) {
        return itemService.update(itemDto, userId, itemId);
    }

    @GetMapping(value = "/search")
    public List<ItemDto> search(@RequestParam(value = "text", defaultValue = "", required = false) String searchBy) {
        return itemService.search(searchBy);
    }

    @PostMapping(value = "/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @PathVariable Long itemId,
                                     @Valid @RequestBody CommentPostRequestDto comment) {
        return itemService.addComment(comment, itemId, userId);
    }

}
