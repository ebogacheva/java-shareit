package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private static final AtomicLong ID_PROVIDER = new AtomicLong(0);
    private final HashMap<Long, Item> items = new HashMap<>();

    @Override
    public Optional<Item> getById(Long itemId) {
        Item item = items.get(itemId);
        if (Objects.nonNull(item)) {
            return Optional.of(item);
        } return Optional.empty();
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public Item create(ItemDto itemDto, User user) {
        Long itemId = ID_PROVIDER.incrementAndGet();
        Item created = ItemMapper.toItem(itemDto, itemId, user);
        items.put(itemId, created);
        return created;
    }

    @Override
    public Optional<Item> update(ItemDto itemDto, Long itemId) {
        Item item = items.get(itemId);
        if (Objects.nonNull(itemDto) && Objects.nonNull(item)) {
            ItemMapper.updateItemWithItemDto(item, itemDto);
            items.put(itemId, item);
        }
        return getById(itemId);
    }

    @Override
    public void delete(Long itemId) {
        items.remove(itemId);
    }
}
