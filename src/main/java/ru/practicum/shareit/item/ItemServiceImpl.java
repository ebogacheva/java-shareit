package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessForbiddenException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final String EXCEPTION_NOT_FOUND_INFO = "Item not found.";
    private static final String EXCEPTION_ACCESS_FORBIDDEN_INFO = "Only owner can change the item.";

    private final ItemRepository itemRepositoryImpl;
    private final UserService userServiceImpl;

    @Override
    public Item create(ItemDto itemDto, Long userId) {
        UserDto user = userServiceImpl.getById(userId);
        return itemRepositoryImpl.create(itemDto, user);
    }

    @Override
    public Item getById(Long itemId) {
        Optional<Item> itemOptional = itemRepositoryImpl.getById(itemId);
        return itemOptional.orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_NOT_FOUND_INFO));
    }

    @Override
    public List<Item> findAll(Long userId) {
        return itemRepositoryImpl.findAll(userId);
    }

    @Override
    public Item update(ItemDto itemDto, Long userId, Long itemId) {
        Item itemInMemory = getById(itemId);
        userServiceImpl.getById(userId);
        if (!Objects.equals(itemInMemory.getOwner().getId(), userId)) {
            throw new AccessForbiddenException(EXCEPTION_ACCESS_FORBIDDEN_INFO);
        }
        Optional<Item> itemOptional = itemRepositoryImpl.update(itemDto, itemId);
        return itemOptional.orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_NOT_FOUND_INFO));
    }

    @Override
    public List<Item> search(String searchBy) {
        return itemRepositoryImpl.search(searchBy);
    }
}
