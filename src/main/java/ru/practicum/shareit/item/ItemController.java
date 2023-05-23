package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

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
    public Item create(@RequestHeader(X_SHARER_USER_ID) long userId,
                       @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(itemDto, userId);
    }

    @GetMapping(value = "/{itemId}")
    public Item getById(@PathVariable Long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<Item> findAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        return itemService.findAll(userId);
    }

    @PatchMapping(value = "/{itemId}")
    public Item update(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable Long itemId,
                       @RequestBody ItemDto itemDto) {
        return itemService.update(itemDto, userId, itemId);
    }

    @GetMapping(value = "/search")
    public List<Item> search(@RequestParam(value = "text", defaultValue = "", required = false) String searchBy) {
        return itemService.search(searchBy);
    }
}
