package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemInRequestDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceRepositoryIntegrationTest {

    private final UserServiceImpl userService;
    private final ItemServiceImpl itemService;
    private final ItemRequestServiceImpl requestService;
    private final ItemRequestRepository requestRepository;

    private Long userId;
    private ItemRequestInputDto itemRequestInputDto;
    private ItemInputDto itemInputDto;
    private Long bookerId;

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

        // default request input
        itemRequestInputDto = ItemRequestInputDto.builder()
                .description("request description")
                .build();
    }

    @Test
    void create_thenReturnRequestWithItemsDto() {
        // saving valid input - creating request
        RequestWithItemsDto savedToDb = requestService.create(itemRequestInputDto, bookerId);
        Long requestId = savedToDb.getId();

        // saving valid item
        itemInputDto.setRequestId(requestId);
        ItemOutDto itemOutDto = itemService.create(itemInputDto, userId);
        Long itemId = itemOutDto.getId();

        // then get request by id from repo
        Optional<ItemRequest> requestOptionalFromDb = requestRepository.findById(requestId);
        ItemRequest itemRequestFromDb = null;
        if (requestOptionalFromDb.isPresent()) {
            itemRequestFromDb = requestOptionalFromDb.get();
        }

        assertNotNull(itemRequestFromDb);
        assertEquals(itemRequestInputDto.getDescription(), itemRequestFromDb.getDescription());

        // when get request from service - with items
        RequestWithItemsDto requestFromService = requestService.getById(bookerId, requestId);
        assertEquals(itemRequestInputDto.getDescription(), requestFromService.getDescription());
        List<ItemInRequestDto> itemsInRequest = requestFromService.getItems();
        assertEquals(itemId, itemsInRequest.get(0).getId());
    }

    @Test
    void findAll_thenReturnListOfRequestDto() {
        // check repo is empty
        List<RequestWithItemsDto> requests = requestService.findAll(bookerId);
        assertEquals(0, requests.size());

        // saving valid input - creating request
        RequestWithItemsDto savedToDb1 = requestService.create(itemRequestInputDto, bookerId);

        // assert results
        requests = requestService.findAll(bookerId);
        assertEquals(1, requests.size());
    }

    @Test
    void getById_thenReturnRequestWithItemsDto() {
        // saving valid input - creating request
        RequestWithItemsDto savedToDb = requestService.create(itemRequestInputDto, bookerId);
        Long requestId = savedToDb.getId();

        // then get request by id from repo
        Optional<ItemRequest> requestOptionalFromDb = requestRepository.findById(requestId);
        ItemRequest itemRequestFromDb = null;
        if (requestOptionalFromDb.isPresent()) {
            itemRequestFromDb = requestOptionalFromDb.get();
        }

        assertNotNull(itemRequestFromDb);
        assertEquals(itemRequestInputDto.getDescription(), itemRequestFromDb.getDescription());
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