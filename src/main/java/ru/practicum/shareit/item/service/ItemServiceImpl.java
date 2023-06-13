package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessForbiddenException;
import ru.practicum.shareit.exception.NoUserBookingAvailableToComment;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.CommentPostRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
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
    private static final String EXCEPTION_BOOKING_NOT_FOUND_INFO = "No booking to comment.";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = getUserIfExists(userId);
        Item itemFromDto = ItemMapper.toItem(itemDto, user);
        return ItemMapper.toItemDto(itemRepository.save(itemFromDto));
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        Item item = getItemIfExists(itemId);
        ItemResponseDto itemResponseDto = ItemMapper.toItemResponseDto(item);
        boolean userIsItemOwner = item.getOwner().getId().equals(userId);
        if (userIsItemOwner) {
            completeItemDtoWithBookingsInfo(itemResponseDto);
            completeItemDtoWithComments(itemResponseDto);
        } else {
            completeItemDtoWithComments(itemResponseDto);
        }
        return itemResponseDto;
    }

    @Override
    public List<ItemResponseDto> findAll(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);
        return items.stream()
                .map(ItemMapper::toItemResponseDto)
                .map(this::completeItemDtoWithBookingsInfo)
                .map(this::completeItemDtoWithComments)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long userId, Long itemId) {
        Item item = getItemIfExists(itemId);
        getUserIfExists(userId);
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

    @Override
    public CommentResponseDto addComment(CommentPostRequestDto commentPostRequestDto, Long itemId, Long userId) {
        Item item = getItemIfExists(itemId);
        User user = getUserIfExists(userId);
        Comment comment = CommentMapper.toComment(commentPostRequestDto, item, user);
        Optional<Booking> userBookingOfItem =
                bookingRepository.findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore(
                        userId, itemId, BookingStatus.APPROVED, LocalDateTime.now()
                );
        if (userBookingOfItem.isPresent()) {
            return CommentMapper.toCommentResponseDto(commentRepository.save(comment));
        } else {
            throw new NoUserBookingAvailableToComment(EXCEPTION_BOOKING_NOT_FOUND_INFO);
        }
    }

    private ItemResponseDto completeItemDtoWithBookingsInfo(ItemResponseDto itemResponseDto) {
        Long itemId = itemResponseDto.getId();
        LocalDateTime now = LocalDateTime.now();
        Sort sortEnds = Sort.by("start").descending();
        Sort sortStarts = Sort.by("start").ascending();
        Optional<Booking> lastBooking = bookingRepository.findFirst1BookingByItemIdAndStatusAndStartBefore(itemId,  BookingStatus.APPROVED, now, sortEnds);
        Optional<Booking> nextBooking = bookingRepository.findFirst1BookingByItemIdAndStatusAndStartAfter(itemId, BookingStatus.APPROVED, now, sortStarts);
        lastBooking.ifPresent(booking -> itemResponseDto.setLastBooking(BookingMapper.toBookingInItemDto(booking)));
        nextBooking.ifPresent(booking -> itemResponseDto.setNextBooking(BookingMapper.toBookingInItemDto(booking)));
        return itemResponseDto;
    }

    private ItemResponseDto completeItemDtoWithComments(ItemResponseDto itemResponseDto) {
        List<Comment> itemComments = commentRepository.findCommentsByItemId(itemResponseDto.getId());
        itemResponseDto.setComments(CommentMapper.toCommentDtoList(itemComments));
        return itemResponseDto;
    }

    private Item getItemIfExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_ITEM_NOT_FOUND_INFO));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_USER_NOT_FOUND_INFO));
    }
}
