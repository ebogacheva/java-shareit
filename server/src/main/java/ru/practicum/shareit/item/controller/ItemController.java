package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemServiceImpl itemService;

    @PostMapping
    public ItemOutDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                             @RequestBody ItemInputDto itemInputDto) {
        return itemService.create(itemInputDto, userId);
    }

    @GetMapping(value = "/{itemId}")
    public ItemFullDto getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                               @PathVariable Long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemFullDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @RequestParam(required = false, defaultValue = "0") int from,
                                     @RequestParam(required = false, defaultValue = "10") int size) {
        return itemService.findAll(userId, from, size);
    }

    @PatchMapping(value = "/{itemId}")
    public ItemOutDto update(@RequestHeader(X_SHARER_USER_ID) long userId,
                             @PathVariable Long itemId,
                             @RequestBody ItemInputDto itemInputDto) {
        return itemService.update(itemInputDto, userId, itemId);
    }

    @GetMapping(value = "/search")
    public List<ItemOutDto> search(@RequestParam(value = "text", defaultValue = "", required = false) String searchBy,
                                   @RequestParam(required = false, defaultValue = "0") int from,
                                   @RequestParam(required = false, defaultValue = "10") int size) {
        return itemService.search(searchBy, from, size);
    }

    @PostMapping(value = "/{itemId}/comment")
    public CommentFullDto addComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @PathVariable Long itemId,
                                     @RequestBody CommentInputDto comment) {
        return itemService.addComment(comment, itemId, userId);
    }

}
