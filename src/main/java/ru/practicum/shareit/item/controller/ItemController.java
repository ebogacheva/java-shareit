package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
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
    public ItemInputDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                               @Valid @RequestBody ItemInputDto itemInputDto) {
        return itemService.create(itemInputDto, userId);
    }

    @GetMapping(value = "/{itemId}")
    public ItemFullDto getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                               @PathVariable Long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemFullDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                     @Min(0) @RequestParam(required = false, defaultValue = "10") int size) {
        return itemService.findAll(userId, from, size);
    }

    @PatchMapping(value = "/{itemId}")
    public ItemInputDto update(@RequestHeader(X_SHARER_USER_ID) long userId,
                               @PathVariable Long itemId,
                               @RequestBody ItemInputDto itemInputDto) {
        return itemService.update(itemInputDto, userId, itemId);
    }

    @GetMapping(value = "/search")
    public List<ItemInputDto> search(@RequestParam(value = "text", defaultValue = "", required = false) String searchBy,
                                     @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                     @Min(0) @RequestParam(required = false, defaultValue = "10") int size) {
        return itemService.search(searchBy, from, size);
    }

    @PostMapping(value = "/{itemId}/comment")
    public CommentFullDto addComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @PathVariable Long itemId,
                                     @Valid @RequestBody CommentInputDto comment) {
        return itemService.addComment(comment, itemId, userId);
    }

}
