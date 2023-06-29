package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    private static final int PAGE_SIZE_20 = 20;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_20 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_20);
    private static final Sort SORT = Sort.by("created").descending();

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRequestRepository requestRepository;

    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;


    @BeforeEach
    public void beforeEach() {
        User user1Input = User.builder()
                .id(null)
                .name("ownerName")
                .email("owner@email.ru")
                .build();

        user1 = userRepository.save(user1Input);

        User user2Input = User.builder()
                .id(null)
                .name("bookerName")
                .email("booker@email.ru")
                .build();

        user2 = userRepository.save(user2Input);

        ItemRequest requestInput1 = ItemRequest.builder()
                .id(null)
                .description("text description_1")
                .requester(user1)
                .created(LocalDateTime.now().minusWeeks(1))
                .build();

        request1 = requestRepository.save(requestInput1);

        ItemRequest requestInput2 = ItemRequest.builder()
                .id(null)
                .description("text description_2")
                .requester(user2)
                .created(LocalDateTime.now().minusWeeks(1))
                .build();

        request2 = requestRepository.save(requestInput2);
    }

    @AfterEach
    public void afterEach() {
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findAllByRequesterId() {
        List<ItemRequest> actual = requestRepository.findAllByRequesterId(user1.getId(), SORT);
        List<ItemRequest> expected = List.of(request1);
        assertEqualLists(expected, actual);
    }

    @Test
    void findAll_whenRequesterIdNotEqualToParam_thenReturnPageOfRequests() {
        List<ItemRequest> actual = requestRepository.findAll(user1.getId(), PAGEABLE_20).getContent();
        List<ItemRequest> expected = List.of(request2);
        assertEqualLists(expected, actual);
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