package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepositoryImpl;

    @Autowired
    private UserService userServiceImpl;

    @Override
    public Item create(ItemDto itemDto, Long userId) {
        User user = userServiceImpl.getById(userId);
        return itemRepositoryImpl.create(itemDto, user);
    }

    @Override
    public Item getById(Long itemId) {
        Optional<Item> itemOptional = itemRepositoryImpl.getById(itemId);
        if (itemOptional.isPresent()) {
            return itemOptional.get();
        } else throw new ShareItElementNotFoundException("Item not found.");
    }

    @Override
    public List<Item> findAll() {
        return itemRepositoryImpl.findAll();
    }

    @Override
    public Item update(ItemDto itemDto, Long itemId) {
        Optional<Item> itemOptional = itemRepositoryImpl.update(itemDto, itemId);
        if (itemOptional.isPresent()) {
            return itemOptional.get();
        } throw new ShareItElementNotFoundException("Item not found.");
    }

    @Override
    public void delete(Long itemId) {
        itemRepositoryImpl.delete(itemId);
    }
}
