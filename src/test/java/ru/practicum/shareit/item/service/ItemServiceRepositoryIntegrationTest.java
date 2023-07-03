package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInItemDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceRepositoryIntegrationTest {

    private static final int PAGE_SIZE_20 = 20;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_20 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_20);
    private static final LocalDateTime START = LocalDateTime.now().minusWeeks(2);
    private static final LocalDateTime END = LocalDateTime.now().minusWeeks(1);

    private final ItemRepository itemRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;
    private final CommentRepository commentRepository;
    private final BookingService bookingService;

    private Long userId;
    private Long bookerId;
    private ItemInputDto itemInputDto;
    private CommentInputDto commentInputDto;
    private BookingInputDto bookingInputDto;

    @BeforeEach
    public void beforeEach() {
        // default owner - saved to db
        UserDto userInputDto = UserDto.builder()
                .id(null)
                .name("userName")
                .email("user@email.ru")
                .build();
        UserDto userDto = userService.create(userInputDto);
        userId = userDto.getId();

        // default booker - saved to db
        UserDto bookerInputDto = UserDto.builder()
                .id(null)
                .name("bookerName")
                .email("booker@email.ru")
                .build();
        UserDto bookerDto = userService.create(bookerInputDto);
        bookerId = bookerDto.getId();

        // default item - available
        itemInputDto = ItemInputDto.builder()
                .id(null)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(userId)
                .requestId(null)
                .build();

        // default booking input (without itemId)
        bookingInputDto = BookingInputDto.builder()
                .id(null)
                .start(START)
                .end(END)
                .itemId(null)
                .status(null)
                .build();

        // default comment input
        commentInputDto = CommentInputDto.builder()
                .text("text comment")
                .build();
    }

    @Test
    void create_thenExistInDb() {
        // saving valid input - creating item
        ItemOutDto expectedDto = itemService.create(itemInputDto, userId);
        Long itemId = expectedDto.getId();

        // then item is saved in db
        Optional<Item> actualItem = itemRepository.findById(itemId);
        assertTrue(actualItem.isPresent());

        ItemOutDto actualDto = ItemMapper.toItemOutDto(actualItem.get());
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void getById_whenByOwner_thenCompletedWithBookingsAndComments() {
        // saving valid input - creating item
        ItemOutDto savedItemDto = itemService.create(itemInputDto, userId);
        Long itemId = savedItemDto.getId();

        // then item get by id
        ItemFullDto itemFullDtoFromService = itemService.getById(userId, itemId);
        Optional<Item> itemOptionalFromRepo = itemRepository.findById(itemId);
        ItemFullDto itemFullDtoFromRepo = null;
        if (itemOptionalFromRepo.isPresent()) {
            itemFullDtoFromRepo = ItemMapper.toItemFullDto(itemOptionalFromRepo.get());
        }
        // then assert equality for fields, item from repo - not null
        assertNotNull(itemFullDtoFromRepo);
        assertEquals(savedItemDto.getName(), itemFullDtoFromRepo.getName());
        assertEquals(savedItemDto.getDescription(), itemFullDtoFromRepo.getDescription());

        // if add approved booking to saved item in the past, booker will be able to add comment
        bookingInputDto.setItemId(itemId);
        BookingFullDto bookingFullDto = bookingService.create(bookingInputDto, bookerId);
        bookingService.setStatus(userId, bookingFullDto.getId(), true);

        // add comment
        CommentFullDto commentFullDto = itemService.addComment(commentInputDto, itemId, bookerId);

        // get by id again - we will have item with past booking and comment when get by owner
        ItemFullDto itemFullDto = itemService.getById(userId, itemId);
        BookingInItemDto lastBookingActual = itemFullDto.getLastBooking();
        BookingInItemDto nextBookingActual = itemFullDto.getNextBooking();
        List<CommentFullDto> commentListActual = itemFullDto.getComments();

        // assert results
        assertThat(lastBookingActual, notNullValue());
        assertThat(nextBookingActual, nullValue());
        assertThat(commentListActual, not(List.of()));
        assertThat(commentListActual.get(0).getText(), equalTo(commentFullDto.getText()));
        assertEquals(bookingFullDto.getId(), lastBookingActual.getId());
    }

    @Test
    void findAll() {
        // saving valid input - creating item
        ItemOutDto savedItemDto1 = itemService.create(itemInputDto, userId);
        ItemOutDto savedItemDto2 = itemService.create(itemInputDto, userId);

        List<ItemFullDto> listOfItemDtoFromService = itemService.findAll(userId, 0, 20);
        assertThat(listOfItemDtoFromService.size(), is(2));
    }

    @Test
    void update() {
        // saving valid input - creating item
        ItemOutDto savedFromDb = itemService.create(itemInputDto, userId);
        Long itemId = savedFromDb.getId();

        // make changes to inputDtoObject
        itemInputDto.setAvailable(false);
        itemInputDto.setName("updated");
        itemInputDto.setDescription(null);

        // then save updated object
        ItemOutDto updatedFromService = itemService.update(itemInputDto, userId, itemId);

        // assert result
        Optional<Item> itemFromRepo = itemRepository.findById(itemId);
        assertTrue(itemFromRepo.isPresent());
        ItemOutDto itemOutDto = ItemMapper.toItemOutDto(itemFromRepo.get());
        assertNotEquals(savedFromDb, updatedFromService);
        assertNotNull(itemOutDto);
    }

    @Test
    void search() {
        // saving valid input - creating item
        itemInputDto.setDescription("for searching");
        ItemOutDto savedFromDb = itemService.create(itemInputDto, userId);
        Long itemId = savedFromDb.getId();

        List<ItemOutDto> actual = itemService.search("search", 0, 20);
        List<ItemOutDto> expected = List.of(savedFromDb);

        assertEquals(expected, actual);
    }

    @Test
    void addComment() {
        // saving valid input - creating item
        ItemOutDto savedItemDto = itemService.create(itemInputDto, userId);
        Long itemId = savedItemDto.getId();

        // if add approved booking to saved item in the past, booker will be able to add comment
        bookingInputDto.setItemId(itemId);
        BookingFullDto bookingFullDto = bookingService.create(bookingInputDto, bookerId);
        bookingService.setStatus(userId, bookingFullDto.getId(), true);

        // add comment
        CommentFullDto commentFullDto = itemService.addComment(commentInputDto, itemId, bookerId);

        // assert results
        Optional<Comment> commentOptionalFromDb = commentRepository.findById(commentFullDto.getId());
        assertTrue(commentOptionalFromDb.isPresent());
        CommentFullDto commentFullDtoFromDb = CommentMapper.toCommentFullDto(commentOptionalFromDb.get());
        assertEquals(commentFullDto, commentFullDtoFromDb);
    }

    private static <T> void assertEqualLists(List<T> expected, List<T> actual) {
        assertListSize(expected, actual);
        assertListsContainAll(expected, actual);
    }

    private static <T> void assertListSize(List<T> expected, List<T> actual) {
        assertEquals(expected.size(), actual.size());
    }

    private static <T> void assertListsContainAll(List<T> expected, List<T> actual) {
        assertTrue(expected.containsAll(actual));
        assertTrue(actual.containsAll(expected));
    }
}