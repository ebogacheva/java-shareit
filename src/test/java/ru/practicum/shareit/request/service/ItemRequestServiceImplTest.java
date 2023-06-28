package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemInRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long ITEM_ID = 1L;
    private static final Long REQUEST_ID_1 = 1L;
    private static final Long REQUEST_ID_2 = 2L;
    private static final LocalDateTime REQUEST_CREATED_1 = LocalDateTime.now().minusWeeks(1);
    private static final LocalDateTime REQUEST_CREATED_2 = LocalDateTime.now().minusWeeks(2);
    private static final Sort SORT = Sort.by("created").descending();
    private static final int START_ELEMENT_INDEX = 0;
    private static final int PAGE_SIZE_1 = 1;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_1 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_1, SORT);
    private static Page<ItemRequest> PAGE_OF_REQUESTS_1;
    private static final int TOTAL_REQUEST_NUMBER = 2;

    private User user;
    private User owner;
    private Item item;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequestInputDto requestInputDto;
    private RequestWithItemsDto requestWithItemsDto_1;
    private RequestWithItemsDto requestWithItemsDto_2;
    private RequestWithItemsDto requestWithoutItemsDto;
    private ItemInRequestDto itemInRequestDto;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @BeforeEach
    void beforeEach() {
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

        request1 = ItemRequest.builder()
                .id(REQUEST_ID_1)
                .description("requestDescription")
                .requester(user)
                .created(REQUEST_CREATED_1)
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(request1)
                .build();

        request1 = ItemRequest.builder()
                .id(REQUEST_ID_1)
                .description("requestDescription")
                .requester(user)
                .created(REQUEST_CREATED_1)
                .build();

        request2 = ItemRequest.builder()
                .id(REQUEST_ID_2)
                .description("requestDescription_2")
                .requester(user)
                .created(REQUEST_CREATED_2)
                .build();

        itemInRequestDto = ItemInRequestDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .requestId(REQUEST_ID_1)
                .build();

        requestInputDto = new ItemRequestInputDto("requestDescription");

        requestWithItemsDto_1 = RequestWithItemsDto.builder()
                .id(REQUEST_ID_1)
                .description("requestDescription")
                .requester(USER_ID)
                .created(REQUEST_CREATED_1)
                .items(List.of(itemInRequestDto))
                .build();

        requestWithItemsDto_2 = RequestWithItemsDto.builder()
                .id(REQUEST_ID_2)
                .description("requestDescription_2")
                .requester(USER_ID)
                .created(REQUEST_CREATED_2)
                .items(List.of())
                .build();

        requestWithoutItemsDto = RequestWithItemsDto.builder()
                .id(REQUEST_ID_1)
                .description("requestDescription")
                .requester(USER_ID)
                .created(REQUEST_CREATED_1)
                .items(null)
                .build();
        PAGE_OF_REQUESTS_1 = spy(new PageImpl<>(List.of(request2), PAGEABLE_1, TOTAL_REQUEST_NUMBER));
    }

    @Test
    void create_whenUserExist_thenReturnRequestDto() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(requestRepository.save(ArgumentMatchers.any(ItemRequest.class))).thenReturn(request1);

        RequestWithItemsDto expected = requestWithoutItemsDto;
        RequestWithItemsDto actual = itemRequestService.create(requestInputDto, USER_ID);

        assertThat(actual, samePropertyValuesAs(expected));
        assertThat(actual).isEqualTo(expected);
        verify(requestRepository, times(1)).save(ArgumentMatchers.any(ItemRequest.class));
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void create_whenUserNotExist_thenThrowNotFound() {
        String expectedMessage = "User not found.";
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class, () -> {
            itemRequestService.create(requestInputDto, USER_ID);
        });

        assertEquals(expectedMessage, actual.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
        verifyNoInteractions(requestRepository);
    }

    @Test
    void findAll_whenUserExist_thenReturnListIfRequestDto() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequesterId(USER_ID, SORT)).thenReturn(List.of(request2, request1));
        when(itemRepository.findAllByRequestId(REQUEST_ID_1)).thenReturn(List.of(item));
        when(itemRepository.findAllByRequestId(REQUEST_ID_2)).thenReturn(List.of());

        List<RequestWithItemsDto> expected = List.of(requestWithItemsDto_2, requestWithItemsDto_1);
        List<RequestWithItemsDto> actual = itemRequestService.findAll(USER_ID);

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i), samePropertyValuesAs(expected.get(i)));
        }
        verify(userRepository, times(1)).findById(USER_ID);
        verify(requestRepository, times(1)).findAllByRequesterId(USER_ID, SORT);
        verify(itemRepository, times(2)).findAllByRequestId(anyLong());
    }

    @Test
    void findAll_whenUserNotExist_thenThrowNotFound() {
        String expectedMessage = "User not found.";
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class, () -> {
            itemRequestService.findAll(USER_ID);
        });

        assertEquals(expectedMessage, actual.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
        verifyNoInteractions(requestRepository);
    }

    @Test
    void testFindAll_whenUserExist_returnRequestDtoInPage() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(requestRepository.findAll(USER_ID, PAGEABLE_1)).thenReturn(PAGE_OF_REQUESTS_1);

        List<RequestWithItemsDto> expected = List.of(requestWithItemsDto_2);
        List<RequestWithItemsDto> actual = itemRequestService.findAll(USER_ID, 0, 1);

        assertEqualLists(expected, actual);
        verify(PAGE_OF_REQUESTS_1, times(1)).getContent();
        verify(userRepository, times(1)).findById(USER_ID);
        verify(requestRepository, times(1)).findAll(USER_ID, PAGEABLE_1);
    }

    @Test
    void getById_whenUserExistRequestExist_thenReturnRequestDtoWithItems() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(requestRepository.findById(REQUEST_ID_1)).thenReturn(Optional.of(request1));
        when(itemRepository.findAllByRequestId(REQUEST_ID_1)).thenReturn(List.of(item));

        RequestWithItemsDto expected = requestWithItemsDto_1;
        RequestWithItemsDto actual = itemRequestService.getById(USER_ID, REQUEST_ID_1);

        assertThat(actual, samePropertyValuesAs(expected));
        verify(userRepository, times(1)).findById(USER_ID);
        verify(itemRepository, times(1)).findAllByRequestId(REQUEST_ID_1);
        verify(requestRepository, times(1)).findById(REQUEST_ID_1);
    }

    @Test
    void getById_whenUserNotExistRequestExist_thenThrowNotFound() {
        String expectedMessage = "User not found.";
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class, () -> {
            itemRequestService.getById(USER_ID, REQUEST_ID_1);
        });

        assertEquals(expectedMessage, actual.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
        verifyNoInteractions(requestRepository);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void getById_whenUserExistRequestNotExist_thenThrowNotFound() {
        String expectedMessage = "Request not found";
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(requestRepository.findById(REQUEST_ID_1)).thenReturn(Optional.empty());

        Exception actual = assertThrows(ShareItElementNotFoundException.class, () -> {
            itemRequestService.getById(USER_ID, REQUEST_ID_1);
        });

        assertEquals(expectedMessage, actual.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
        verify(requestRepository, times(1)).findById(REQUEST_ID_1);
        verifyNoInteractions(itemRepository);
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

