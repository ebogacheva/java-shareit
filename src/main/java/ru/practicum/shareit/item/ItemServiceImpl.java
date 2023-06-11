package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingInItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.exception.AccessForbiddenException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final String EXCEPTION_USER_NOT_FOUND_INFO = "User not found.";
    private static final String EXCEPTION_ITEM_NOT_FOUND_INFO = "Item not found.";
    private static final String EXCEPTION_ACCESS_FORBIDDEN_INFO = "Only owner can change the item.";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final UserService userServiceImpl;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_USER_NOT_FOUND_INFO));
        Item itemFromDto = ItemMapper.toItem(itemDto, user);
        return ItemMapper.toItemDto(itemRepository.save(itemFromDto));
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        Item item = findById(itemId);
        ItemResponseDto itemResponseDto = ItemMapper.toItemResponseDto(item);
        if (item.getOwner().getId().equals(userId)) {
            return completeItemDtoWithBookingsInfo(itemResponseDto, userId);
        }
        return itemResponseDto;
    }

    @Override
    public List<ItemResponseDto> findAll(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);
        return items.stream()
                .map(ItemMapper::toItemResponseDto)
                .map(it -> completeItemDtoWithBookingsInfo(it, userId))
                .collect(Collectors.toList());
    }

    private ItemResponseDto completeItemDtoWithBookingsInfo(ItemResponseDto itemResponseDto, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Sort sortEnds = Sort.by("end").descending();
        Sort sortStarts = Sort.by("start").ascending();
        Booking lastBooking = bookingRepository.findFirst1BookingByItemIdAndEndBefore(itemResponseDto.getId(),  now, sortEnds);
        Booking nextBooking = bookingRepository.findFirst1BookingByItemIdAndStartAfter(itemResponseDto.getId(), now, sortStarts);
        if (Objects.nonNull(lastBooking)) {
            itemResponseDto.setLastBooking(BookingMapper.toBookingInItemDto(lastBooking));
        }
        if (Objects.nonNull(nextBooking)) {
            itemResponseDto.setNextBooking(BookingMapper.toBookingInItemDto(nextBooking));
        }
        return itemResponseDto;
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long userId, Long itemId) {
        Item item = findById(itemId);
        userServiceImpl.existsById(userId);
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new AccessForbiddenException(EXCEPTION_ACCESS_FORBIDDEN_INFO);
        }
        ItemMapper.updateItemWithItemDto(item, itemDto);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> search(String searchBy) {
        if (searchBy.isBlank()) {
            return List.of();
        }
        return ItemMapper.toItemDtoList(itemRepository.search(searchBy));
    }

    private Item findById(Long itemId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        return itemOptional.orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_ITEM_NOT_FOUND_INFO));
    }
}
