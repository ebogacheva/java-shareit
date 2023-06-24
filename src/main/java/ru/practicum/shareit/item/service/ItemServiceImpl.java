package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessForbiddenException;
import ru.practicum.shareit.exception.NoUserBookingAvailableToComment;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
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
    private static final String EXCEPTION_REQUEST_NOT_FOUND_INFO = "Request not found";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemOutDto create(ItemInputDto itemInputDto, Long userId) {
        User user = getUserIfExists(userId);
        ItemRequest request = null;
        if (Objects.nonNull(itemInputDto.getRequestId())) {
            request = getItemRequestIfExists(itemInputDto.getRequestId());
        }
        Item itemFromDto = ItemMapper.toItem(itemInputDto, user, request);
        return ItemMapper.toItemOutDto(itemRepository.save(itemFromDto));
    }

    @Override
    public ItemFullDto getById(Long userId, Long itemId) {
        Item item = getItemIfExists(itemId);
        ItemFullDto itemFullDto = ItemMapper.toItemResponseDto(item);
        boolean isUserItemOwner = item.getOwner().getId().equals(userId);
        if (isUserItemOwner) {
            completeItemDtoWithBookingsInfo(itemFullDto);
            completeItemDtoWithComments(itemFullDto);
        } else {
            completeItemDtoWithComments(itemFullDto);
        }
        return itemFullDto;
    }

    @Override
    public List<ItemFullDto> findAll(Long userId, int from, int size) {
        Page<Item> itemPages = itemRepository.findAllByOwnerIdOrderByIdAsc(userId, pageRequestOf(from, size));
        return itemPages.stream()
                .map(ItemMapper::toItemResponseDto)
                .map(this::completeItemDtoWithBookingsInfo)
                .map(this::completeItemDtoWithComments)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemOutDto update(ItemInputDto itemInputDto, Long userId, Long itemId) {
        Item item = getItemIfExists(itemId);
        getUserIfExists(userId);
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new AccessForbiddenException(EXCEPTION_ACCESS_FORBIDDEN_INFO);
        }
        ItemMapper.updateItemWithItemDto(item, itemInputDto);
        return ItemMapper.toItemOutDto(itemRepository.save(item));
    }

    @Override
    public List<ItemOutDto> search(String searchBy, int from, int size) {
        if (searchBy.isBlank()) {
            return List.of();
        }
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        return ItemMapper.toItemDtoList(itemRepository.search(searchBy, pageable));
    }

    @Override
    @Transactional
    public CommentFullDto addComment(CommentInputDto commentPostRequestDto, Long itemId, Long userId) {
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

    private ItemFullDto completeItemDtoWithBookingsInfo(ItemFullDto itemFullDto) {
        Long itemId = itemFullDto.getId();
        LocalDateTime now = LocalDateTime.now();
        Sort sortEnds = Sort.by("start").descending();
        Sort sortStarts = Sort.by("start").ascending();
        Optional<Booking> lastBooking = bookingRepository
                .findFirst1BookingByItemIdAndStatusAndStartBefore(itemId, BookingStatus.APPROVED, now, sortEnds);
        Optional<Booking> nextBooking = bookingRepository
                .findFirst1BookingByItemIdAndStatusAndStartAfter(itemId, BookingStatus.APPROVED, now, sortStarts);
        lastBooking.ifPresent(booking -> itemFullDto.setLastBooking(BookingMapper.toBookingInItemDto(booking)));
        nextBooking.ifPresent(booking -> itemFullDto.setNextBooking(BookingMapper.toBookingInItemDto(booking)));
        return itemFullDto;
    }

    private ItemFullDto completeItemDtoWithComments(ItemFullDto itemFullDto) {
        List<Comment> itemComments = commentRepository.findCommentsByItemId(itemFullDto.getId());
        itemFullDto.setComments(CommentMapper.toCommentDtoList(itemComments));
        return itemFullDto;
    }

    private Item getItemIfExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_ITEM_NOT_FOUND_INFO));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_USER_NOT_FOUND_INFO));
    }

    private ItemRequest getItemRequestIfExists(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_REQUEST_NOT_FOUND_INFO));
    }

    private static Pageable pageRequestOf(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
