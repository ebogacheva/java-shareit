package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private static final AtomicLong ID_PROVIDER = new AtomicLong(0);
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Optional<Item> getById(Long itemId) {
        Item item = items.get(itemId);
        if (Objects.nonNull(item)) {
            return Optional.of(item);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findAll(Long userId) {
        return items.values()
                .stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
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
        }
        return getById(itemId);
    }

    @Override
    public List<Item> search(String searchBy) {
        if (searchBy.isEmpty()) {
            return List.of();
        } else {
            return items.values().stream()
                    .filter(item -> isContainingSearchByText(item, searchBy))
                    .filter(Item::isAvailable)
                    .collect(Collectors.toList());
        }
    }

    private boolean isContainingSearchByText(Item item, String searchBy) {
        String searchInLowerCase = searchBy.toLowerCase();
        String nameInLowerCase = item.getName().toLowerCase();
        String descriptionInLowerCase = item.getDescription().toLowerCase();
        return nameInLowerCase.contains(searchInLowerCase) || descriptionInLowerCase.contains(searchInLowerCase);
    }

}
