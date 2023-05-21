package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    public Item create(@RequestHeader("X-Later-User-Id") Long userId,
                       @RequestBody ItemDto itemDto) {
        return itemService.create(itemDto, userId);
    }

    @GetMapping(value = "/{itemId}")
    public Item getById(@PathVariable Long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<Item> findAll() {
        return itemService.findAll();
    }

    @PatchMapping(value = "/{itemId}")
    public Item update(@PathVariable Long itemId,
                       @RequestBody ItemDto itemDto) {
        return itemService.update(itemDto, itemId);
    }

    @DeleteMapping(value = "/{itemId}")
    public void delete(@PathVariable Long itemId) {
        itemService.delete(itemId);
    }

}
