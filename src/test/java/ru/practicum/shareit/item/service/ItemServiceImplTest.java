package ru.practicum.shareit.item.service;

import org.hibernate.annotations.CreationTimestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long ITEM_ID = 1L;
    private static final Long REQUEST_ID = 1L;
    private static final LocalDateTime REQUEST_CREATED = LocalDateTime.now().minusWeeks(1);

    private static ItemInputDto itemInputDto;
    private static User user;
    private static User owner;
    private static Item item;
    private static Booking booking;
    private static ItemRequest request;

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


    }

    @Test
    void createItem_whenUserExistRequestNotNull_thenReturnItemDto() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user));
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(itemRepository.save(ArgumentMatchers.any(Item.class))).thenReturn(item);

        ItemOutDto actual = itemService.create(itemInputDto, OWNER_ID);
        ItemOutDto expected = ItemMapper.toItemOutDto(item);

        assertThat(actual, samePropertyValuesAs(expected));
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(requestRepository, times(1)).findById(ITEM_ID);
    }

    @Test
    void createItem_whenUserExistRequestIsNull_thenReturnItemDto() {
        itemInputDto.setRequestId(null);
        item.setRequest(null);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(user));
        when(itemRepository.save(ArgumentMatchers.any(Item.class))).thenReturn(item);

        ItemOutDto actual = itemService.create(itemInputDto, OWNER_ID);
        ItemOutDto expected = ItemMapper.toItemOutDto(item);

        assertThat(actual, samePropertyValuesAs(expected));
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(requestRepository, never()).findById(ITEM_ID);
    }

    @Test
    void createItem_whenUserNotExistRequestNotNull_thenThrowNotFound() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ShareItElementNotFoundException.class, () -> itemService.create(itemInputDto, OWNER_ID));

        String messageExpected = "User not found.";
        assertThat(exception.getMessage(), is(messageExpected));
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(requestRepository, never()).findById(ITEM_ID);
        verify(itemRepository, never()).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    void createItem_whenUserExistRequestNotExist_thenThrowNotFound() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ShareItElementNotFoundException.class, () -> itemService.create(itemInputDto, OWNER_ID));

        String messageExpected = "Request not found";
        assertThat(exception.getMessage(), is(messageExpected));
        verify(userRepository, times(1)).findById(OWNER_ID);
        verify(requestRepository, times(1)).findById(ITEM_ID);
        verify(itemRepository, never()).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    void getById() {
    }

    @Test
    void findAll() {
    }

    @Test
    void update() {
    }

    @Test
    void search() {
    }

    @Test
    void addComment() {
    }
}