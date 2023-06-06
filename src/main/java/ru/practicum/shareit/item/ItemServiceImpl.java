package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessForbiddenException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.UserService;

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
    public ItemDto create(ItemDto itemDto, Long userId) {
        userServiceImpl.existsById(userId);
        Item itemFromDto = ItemMapper.toItem(itemDto, null, userId);
        return ItemMapper.toItemDto(itemRepositoryImpl.save(itemFromDto));
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = findById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findAll(Long userId) {
        return ItemMapper.toItemDtoList(itemRepositoryImpl.findAll());
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long userId, Long itemId) {
        Item item = findById(itemId);
        userServiceImpl.existsById(userId);
        if (!Objects.equals(item.getOwner(), userId)) {
            throw new AccessForbiddenException(EXCEPTION_ACCESS_FORBIDDEN_INFO);
        }
        ItemMapper.updateItemWithItemDto(item, itemDto);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<Item> search(String searchBy) {
        return itemRepositoryImpl.search(searchBy);
    }

    private Item findById(Long itemId) {
        Optional<Item> itemOptional = itemRepositoryImpl.findById(itemId);
        return itemOptional.orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_NOT_FOUND_INFO));
    }
}
