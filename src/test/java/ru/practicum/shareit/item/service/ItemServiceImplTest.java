package ru.practicum.shareit.item.service;

import org.apache.logging.log4j.util.Strings;
import org.hibernate.result.NoMoreReturnsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingInItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessForbiddenException;
import ru.practicum.shareit.exception.NoUserBookingAvailableToComment;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;

import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long OTHER_ID = 3L;
    private static final Long ITEM_ID = 1L;
    private static final Long REQUEST_ID = 1L;
    private static final LocalDateTime REQUEST_CREATED = LocalDateTime.now().minusWeeks(1);
    private static final LocalDateTime LAST_BOOKING_START = LocalDateTime.now().minusWeeks(2);
    private static final LocalDateTime LAST_BOOKING_END = LocalDateTime.now().minusWeeks(1);
    private static final LocalDateTime NEXT_BOOKING_START = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime NEXT_BOOKING_END = LocalDateTime.now().plusWeeks(2);
    private static final Long LAST_BOOKING_ID = 10L;
    private static final Long NEXT_BOOKING_ID = 15L;
    private static final Long COMMENT_ID = 1L;
    private static final LocalDateTime COMMENT_CREATED = LocalDateTime.now().minusDays(1);
    private static final int START_ELEMENT_INDEX = 0;
    private static final int PAGE_SIZE_1 = 1;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_1 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_1);
    private static Page<Item> PAGE_OF_ITEMS_1;
    private static Page<Item> PAGE_OF_ITEMS_EMPTY;
    private static final Long TOTAL_ITEMS_NUMBER = 1L;

    private ItemInputDto itemInputDto;
    private User user;
    private User owner;
    private Item item;
    private ItemRequest request;
    private ItemOutDto itemOutDto;
    private ItemFullDto itemFullDtoForOwner;
    private ItemFullDto itemFullDtoAllUsers;
    private CommentFullDto commentFullDto;
    private CommentInputDto commentInputDto;
    private Comment comment;
    private BookingInItemDto lastBooking;
    private BookingInItemDto nextBooking;
    private Booking last;
    private Booking next;

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Captor
    ArgumentCaptor<Item> itemCaptor;

    @BeforeEach
    void beforeEach() {
        itemService = new ItemServiceImpl(
                itemRepository,
                userRepository,
                bookingRepository,
                commentRepository,
                requestRepository
        );
        itemInputDto = ItemInputDto.builder()
                .id(null)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(OWNER_ID)
                .requestId(REQUEST_ID)
                .build();

        user = User.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();

        owner = User.builder()
                .id(OWNER_ID)
                .name("ownerName")
                .email("owner@email.ru")
                .build();

        request = ItemRequest.builder()
                .id(REQUEST_ID)
                .description("requestDescription")
                .requester(user)
                .created(REQUEST_CREATED)
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        last = Booking.builder()
                .id(LAST_BOOKING_ID)
                .start(LAST_BOOKING_START)
                .end(LAST_BOOKING_END)
                .booker(user)
                .build();

        lastBooking = BookingInItemDto.builder()
                .id(LAST_BOOKING_ID)
                .start(LAST_BOOKING_START)
                .end(LAST_BOOKING_END)
                .bookerId(USER_ID)
                .build();

        next = Booking.builder()
                .id(NEXT_BOOKING_ID)
                .start(NEXT_BOOKING_START)
                .end(NEXT_BOOKING_END)
                .booker(user)
                .build();

        nextBooking = BookingInItemDto.builder()
                .id(NEXT_BOOKING_ID)
                .start(NEXT_BOOKING_START)
                .end(NEXT_BOOKING_END)
                .bookerId(USER_ID)
                .build();

        comment = Comment.builder()
                .id(COMMENT_ID)
                .text("comment text")
                .item(item)
                .author(user)
                .created(COMMENT_CREATED)
                .build();

        commentFullDto = CommentFullDto.builder()
                .id(COMMENT_ID)
                .text("comment text")
                .itemId(ITEM_ID)
                .authorName("userName")
                .created(COMMENT_CREATED)
                .build();

        commentInputDto = CommentInputDto.builder()
                .text("comment text")
                .build();

        itemFullDtoForOwner = ItemFullDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(commentFullDto))
                .build();

        itemFullDtoAllUsers = ItemFullDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of(commentFullDto))
                .build();

        itemOutDto = ItemOutDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(OWNER_ID)
                .requestId(REQUEST_ID)
                .build();
        PAGE_OF_ITEMS_1 = new PageImpl<>(List.of(item), PAGEABLE_1, TOTAL_ITEMS_NUMBER);
        PAGE_OF_ITEMS_EMPTY = new PageImpl<>(List.of(), PAGEABLE_1, 0);
    }

    @Test
    void createItem_whenUserExistRequestNotNull_thenReturnItemDto() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(itemRepository.save(ArgumentMatchers.any(Item.class))).thenReturn(item);

        ItemOutDto actual = itemService.create(itemInputDto, OWNER_ID);
        ItemOutDto expected = itemOutDto;

        assertThat(actual, samePropertyValuesAs(expected));
        assertThat(actual).isEqualTo(itemOutDto);
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(requestRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    void createItem_whenUserExistRequestIsNull_thenReturnItemDto() {
        itemInputDto.setRequestId(null);
        item.setRequest(null);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(itemRepository.save(ArgumentMatchers.any(Item.class))).thenReturn(item);

        ItemOutDto actual = itemService.create(itemInputDto, OWNER_ID);
        ItemOutDto expected = ItemMapper.toItemOutDto(item);

        assertThat(actual, samePropertyValuesAs(expected));
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(requestRepository, never()).findById(ITEM_ID);
    }

    @Test
    void createItem_whenUserNotExistRequestNotNull_thenThrowNotFound() {
        String messageExpected = "User not found.";
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ShareItElementNotFoundException.class,
                () -> itemService.create(itemInputDto, OWNER_ID));

        assertThat(exception.getMessage(), is(messageExpected));
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(requestRepository, never()).findById(ITEM_ID);
        verify(itemRepository, never()).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    void createItem_whenUserExistRequestNotExist_thenThrowNotFound() {
        String messageExpected = "Request not found";
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ShareItElementNotFoundException.class,
                () -> itemService.create(itemInputDto, OWNER_ID));

        assertThat(exception.getMessage(), is(messageExpected));
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(requestRepository, times(1)).findById(ITEM_ID);
        verify(itemRepository, never()).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    void getById_whenUserIsOwnerItemExist_thenReturnFullItemDtoForOwner() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirst1BookingByItemIdAndStatusAndStartBefore(
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.any(Sort.class)
        )).thenReturn(Optional.of(last));
        when(bookingRepository.findFirst1BookingByItemIdAndStatusAndStartAfter(
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.any(Sort.class)
        )).thenReturn(Optional.of(next));
        when(commentRepository.findCommentsByItemId(ITEM_ID)).thenReturn(List.of(comment));

        ItemFullDto actual = itemService.getById(OWNER_ID, ITEM_ID);
        ItemFullDto expected = itemFullDtoForOwner;

        assertThat(expected, samePropertyValuesAs(actual));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getById_whenUserIsOwnerItemExistEmptyBookingsNullCommentsNull_thenReturnFullItemDtoForOwner() {
        itemFullDtoForOwner.setLastBooking(null);
        itemFullDtoForOwner.setNextBooking(null);
        itemFullDtoForOwner.setComments(List.of());
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(bookingRepository.findFirst1BookingByItemIdAndStatusAndStartBefore(
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.any(Sort.class)
        )).thenReturn(Optional.empty());
        when(bookingRepository.findFirst1BookingByItemIdAndStatusAndStartAfter(
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.any(Sort.class)
        )).thenReturn(Optional.empty());
        when(commentRepository.findCommentsByItemId(ITEM_ID)).thenReturn(List.of());

        ItemFullDto actual = itemService.getById(OWNER_ID, ITEM_ID);
        ItemFullDto expected = itemFullDtoForOwner;

        assertThat(expected, samePropertyValuesAs(actual));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getById_whenUserIsNotOwnerItemExist_thenReturnFullItemDtoAllUsers() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(commentRepository.findCommentsByItemId(ITEM_ID)).thenReturn(List.of(comment));

        ItemFullDto actual = itemService.getById(USER_ID, ITEM_ID);
        ItemFullDto expected = itemFullDtoAllUsers;

        assertThat(expected, samePropertyValuesAs(actual));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getById_whenItemNotExist_thenThrowNotFound() {
        String expectedMessage = "Item not found.";
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class,
                () -> itemService.getById(USER_ID, ITEM_ID));

        assertEquals(expectedMessage, actual.getMessage());
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void findAll_whenItemsExistBookingsExistCommentsExist_thenReturnItemFullDtoForOwner() {
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(OWNER_ID, PAGEABLE_1)).thenReturn(PAGE_OF_ITEMS_1);
        when(bookingRepository.findFirst1BookingByItemIdAndStatusAndStartBefore(
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.any(Sort.class)
        )).thenReturn(Optional.of(last));
        when(bookingRepository.findFirst1BookingByItemIdAndStatusAndStartAfter(
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.any(Sort.class)
        )).thenReturn(Optional.of(next));
        when(commentRepository.findCommentsByItemId(ITEM_ID)).thenReturn(List.of(comment));

        List<ItemFullDto> expected = List.of(itemFullDtoForOwner);
        List<ItemFullDto> actual = itemService.findAll(OWNER_ID, START_ELEMENT_INDEX, PAGE_SIZE_1);

        assertTrue(expected.size() == actual.size()
                && expected.containsAll(actual)
                && actual.containsAll(expected));
        verify(itemRepository, times(1)).findAllByOwnerIdOrderByIdAsc(OWNER_ID, PAGEABLE_1);
    }

    @Test
    void findAll_whenItemsNotExist_thenReturnEmptyList() {
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(OWNER_ID, PAGEABLE_1)).thenReturn(Page.empty());

        List<ItemFullDto> expected = List.of();
        List<ItemFullDto> actual = itemService.findAll(OWNER_ID, START_ELEMENT_INDEX, PAGE_SIZE_1);

        assertTrue(actual.size() == 0
                && expected.containsAll(actual)
                && actual.containsAll(expected));
        verify(itemRepository, times(1)).findAllByOwnerIdOrderByIdAsc(OWNER_ID, PAGEABLE_1);
        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void update_whenUserIsOwnerItemExist_thenReturnUpdated() {
        String changed = "updated description";
        itemInputDto.setDescription(changed);
        itemOutDto.setDescription(changed);
        item.setDescription(changed);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(itemRepository.save(itemCaptor.capture())).thenReturn(item);

        ItemOutDto expected = itemOutDto;
        ItemOutDto actual = itemService.update(itemInputDto, OWNER_ID, ITEM_ID);

        assertThat(expected, samePropertyValuesAs(actual));
        assertThat(actual).isEqualTo(expected);
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(itemRepository, times(1)).save(item);
        Item shouldBeUpdatedWithChangedDescription = itemCaptor.getValue();
        assertEquals(item, shouldBeUpdatedWithChangedDescription);
    }

    @Test
    void update_whenUserIsNotOwnerItemExist_thenThrowAccessForbidden() {
        String changed = "updated description";
        itemInputDto.setDescription(changed);
        itemOutDto.setDescription(changed);
        item.setDescription(changed);
        String expectedMessage = "Only owner can change the item.";
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Exception actual = assertThrows(AccessForbiddenException.class,
                () -> itemService.update(itemInputDto, USER_ID, ITEM_ID));

        assertThat(expectedMessage, samePropertyValuesAs(actual.getMessage()));
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(userRepository, times(1)).findById(USER_ID);
        verify(itemRepository, never()).save(item);
    }

    @Test
    void update_whenItemNotExist_thenThrowNotFound() {
        String changed = "updated description";
        itemInputDto.setDescription(changed);
        String expectedMessage = "Item not found.";
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class,
                () -> itemService.update(itemInputDto, USER_ID, ITEM_ID));

        assertThat(expectedMessage, samePropertyValuesAs(actual.getMessage()));
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(userRepository, never()).findById(USER_ID);
        verify(itemRepository, never()).save(item);
    }

    @Test
    void update_whenUserNotExist_thenThrowNotFound() {
        String changed = "updated description";
        itemInputDto.setDescription(changed);
        String expectedMessage = "User not found.";
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(userRepository.findById(OTHER_ID)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class,
                () -> itemService.update(itemInputDto, OTHER_ID, ITEM_ID));

        assertEquals(expectedMessage, actual.getMessage());
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(userRepository, times(1)).findById(OTHER_ID);
        verify(itemRepository, never()).save(item);
    }

    @Test
    void search_whenThereIsResult_thenReturnListOfItems() {
        String searchBy = "item";
        when(itemRepository.search(searchBy, PAGEABLE_1)).thenReturn(PAGE_OF_ITEMS_1);

        List<ItemOutDto> actual = itemService.search(searchBy, START_ELEMENT_INDEX, PAGE_SIZE_1);
        List<ItemOutDto> expected = List.of(itemOutDto);

        assertTrue(actual.size() == expected.size()
                && expected.containsAll(actual)
                && actual.containsAll(expected));
        verify(itemRepository, times(1)).search(searchBy, PAGEABLE_1);
    }

    @Test
    void search_whenResultIsEmpty_thenReturnEmptyList() {
        String searchBy = "EMPTY";
        when(itemRepository.search(searchBy, PAGEABLE_1)).thenReturn(PAGE_OF_ITEMS_EMPTY);

        List<ItemOutDto> actual = itemService.search(searchBy, START_ELEMENT_INDEX, PAGE_SIZE_1);
        List<ItemOutDto> expected = List.of();

        assertTrue(actual.size() == 0
                && expected.containsAll(actual)
                && actual.containsAll(expected));
        verify(itemRepository, times(1)).search(searchBy, PAGEABLE_1);
    }

    @Test
    void search_whenSearchIsBlank_thenReturnEmptyList() {
        String searchBy = Strings.EMPTY;

        List<ItemOutDto> actual = itemService.search(searchBy, START_ELEMENT_INDEX, PAGE_SIZE_1);
        List<ItemOutDto> expected = List.of();

        assertTrue(actual.size() == 0
                && expected.containsAll(actual)
                && actual.containsAll(expected));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void addComment_whenItemExistUserExistUserIsBooker_thenSaveAndRetunNewComment() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(bookingRepository.findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore(
                anyLong(),
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(Optional.of(last));
        when(commentRepository.save(ArgumentMatchers.any(Comment.class))).thenReturn(comment);

        CommentFullDto expected = commentFullDto;
        CommentFullDto actual = itemService.addComment(commentInputDto, ITEM_ID, USER_ID);

        assertThat(actual, samePropertyValuesAs(expected));
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(userRepository, times(1)).findById(USER_ID);
        verify(bookingRepository, times(1))
                .findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore(
                anyLong(),
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class)
        );
        verify(commentRepository, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    void addComment_whenItemNotExist_thenThrowNotFound() {
        String expectedMessage = "Item not found.";
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
        Exception actual = assertThrows(ShareItElementNotFoundException.class,
                () -> itemService.addComment(commentInputDto, ITEM_ID, USER_ID));

        assertEquals(expectedMessage, actual.getMessage());
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(userRepository, never()).findById(USER_ID);
        verify(bookingRepository, never())
                .findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore(
                        anyLong(),
                        anyLong(),
                        ArgumentMatchers.any(BookingStatus.class),
                        ArgumentMatchers.any(LocalDateTime.class)
                );
        verify(commentRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void addComment_whenItemExistUserIsNotBooker_thenThrowNotFound() {
        String expectedMessage = "No booking to comment.";
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(bookingRepository.findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore(
                anyLong(),
                anyLong(),
                ArgumentMatchers.any(BookingStatus.class),
                ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        Exception actual = assertThrows(NoUserBookingAvailableToComment.class,
                () -> itemService.addComment(commentInputDto, ITEM_ID, USER_ID));

        assertEquals(expectedMessage, actual.getMessage());
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(userRepository, times(1)).findById(USER_ID);
        verify(bookingRepository, times(1))
                .findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore(
                        anyLong(),
                        anyLong(),
                        ArgumentMatchers.any(BookingStatus.class),
                        ArgumentMatchers.any(LocalDateTime.class)
                );
        verify(commentRepository, never()).save(ArgumentMatchers.any());
    }
}