package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    private static final int PAGE_SIZE_20 = 20;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_20 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_20);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private Item item1;
    private Item item2;
    private User owner;
    private User booker;
    private ItemRequest request;

    @BeforeEach
    public void beforeEach() {
        User ownerInput = User.builder()
                .id(null)
                .name("ownerName")
                .email("owner@email.ru")
                .build();

        owner = userRepository.save(ownerInput);

        User bookerInput = User.builder()
                .id(null)
                .name("bookerName")
                .email("booker@email.ru")
                .build();

        booker = userRepository.save(bookerInput);

        ItemRequest requestInput = ItemRequest.builder()
                .id(null)
                .description("text description")
                .requester(booker)
                .created(LocalDateTime.now().minusWeeks(1))
                .build();

        request = requestRepository.save(requestInput);

        Item itemInput1 = Item.builder()
                .id(null)
                .name("itemName_first_searchingPhrase")
                .description("itemDescription_first")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        item1 = itemRepository.save(itemInput1);

        Item itemInput2 = Item.builder()
                .id(null)
                .name("itemName_second")
                .description("itemDescription_second_lookingForPhrase")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        item2 = itemRepository.save(itemInput2);
    }

    @AfterEach
    public void afterEach() {
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void search_thenReturnPageOfItems() {
        List<Item> actual1 = itemRepository.search("searchingPHRASE", PAGEABLE_20).getContent();
        List<Item> expected1 = List.of(item1);
        assertEqualLists(expected1, actual1);

        List<Item> actual2 = itemRepository.search("empty", PAGEABLE_20).getContent();
        List<Item> expected2 = List.of();
        assertEqualLists(expected2, actual2);
    }

    @Test
    void findAllByOwnerIdOrderByIdAsc_thenReturnPageOfItems() {
        List<Item> actual1 = itemRepository.findAllByOwnerIdOrderByIdAsc(owner.getId(), PAGEABLE_20).getContent();
        List<Item> expected1 = List.of(item1, item2);
        assertEqualLists(expected1, actual1);

        List<Item> actual2 = itemRepository.findAllByOwnerIdOrderByIdAsc(booker.getId(), PAGEABLE_20).getContent();
        List<Item> expected2 = List.of();
        assertEqualLists(expected2, actual2);
    }


    @Test
    void findAllByRequestId_thenReturnListOfItems() {
        List<Item> actual1 = itemRepository.findAllByRequestId(request.getId());
        List<Item> expected1 = List.of(item1, item2);
        assertEqualLists(expected1, actual1);
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