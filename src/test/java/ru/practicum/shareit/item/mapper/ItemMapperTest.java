package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long ITEM_ID = 1L;
    private static final Long REQUEST_ID = 1L;
    private static final LocalDateTime REQUEST_CREATED = LocalDateTime.now().minusWeeks(1);
    private static final int PAGE_SIZE_1 = 1;
    private static final int PAGE_INDEX = 0;
    private static final Pageable PAGEABLE_1 = PageRequest.of(PAGE_INDEX, PAGE_SIZE_1);
    private static Page<Item> PAGE_OF_ITEMS_1;
    private static final Long TOTAL_ITEMS_NUMBER = 1L;

    private ItemInputDto itemInputDto;
    private User owner;
    private Item item;
    private Item itemInput;
    private ItemRequest request;
    private ItemOutDto itemOutDto;
    private ItemFullDto itemFullDto;
    private ItemInputDto updateNameItemInputDto;
    private ItemInputDto updateDescriptionItemInputDto;
    private ItemInputDto updateAvailableItemInputDto;
    private ItemInRequestDto itemInRequestDto;

    @BeforeEach
    void beforeEach() {
        User user = User.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();

        owner = User.builder()
                .id(OWNER_ID)
                .name("ownerName")
                .email("owner@email.ru")
                .build();

        itemInRequestDto = ItemInRequestDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .requestId(REQUEST_ID)
                .build();

        request = ItemRequest.builder()
                .id(REQUEST_ID)
                .description("requestDescription")
                .requester(user)
                .created(REQUEST_CREATED)
                .build();

        itemInputDto = ItemInputDto.builder()
                .id(null)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner.getId())
                .requestId(REQUEST_ID)
                .build();

        itemInput = Item.builder()
                .id(null)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        itemFullDto = ItemFullDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .comments(null)
                .build();

        itemOutDto = ItemOutDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(OWNER_ID)
                .requestId(REQUEST_ID)
                .build();

        updateNameItemInputDto = ItemInputDto.builder()
                .name("updated")
                .build();

        updateDescriptionItemInputDto = ItemInputDto.builder()
                .description("updated")
                .build();

        updateAvailableItemInputDto = ItemInputDto.builder()
                .available(false)
                .build();

        PAGE_OF_ITEMS_1 = new PageImpl<>(List.of(item), PAGEABLE_1, TOTAL_ITEMS_NUMBER);
    }

    @Test
    void toItemOutDto() {
        ItemOutDto expected = itemOutDto;
        ItemOutDto actual = ItemMapper.toItemOutDto(item);
        assertEquals(expected, actual);
    }

    @Test
    void toItemFullDto() {
        ItemFullDto expected = itemFullDto;
        ItemFullDto actual = ItemMapper.toItemFullDto(item);
        assertEquals(expected, actual);
        assertNull(actual.getComments());
        assertNull(actual.getLastBooking());
        assertNull(actual.getNextBooking());
    }

    @Test
    void updateItemWithItemDto_whenUpdateName_thenReturnItemUpdated() {
        String itemNameBeforeUpdate = item.getName();
        String itemNameAfterUpdateExpected = updateNameItemInputDto.getName();

        ItemMapper.updateItemWithItemDto(item, updateNameItemInputDto);
        String itemNameAfterUpdatedActual = item.getName();
        assertEquals(itemNameAfterUpdateExpected, itemNameAfterUpdatedActual);
        assertNotEquals(itemNameBeforeUpdate, itemNameAfterUpdatedActual);
    }

    @Test
    void updateItemWithItemDto_whenUpdateDescription_thenReturnItemUpdated() {
        String itemDescriptionBeforeUpdate = item.getDescription();
        String itemDescriptionAfterUpdateExpected = updateDescriptionItemInputDto.getDescription();

        ItemMapper.updateItemWithItemDto(item, updateDescriptionItemInputDto);
        String itemDescriptionAfterUpdatedActual = item.getDescription();
        assertEquals(itemDescriptionAfterUpdateExpected, itemDescriptionAfterUpdatedActual);
        assertNotEquals(itemDescriptionBeforeUpdate, itemDescriptionAfterUpdatedActual);
    }

    @Test
    void updateItemWithItemDto_whenUpdateAvailable_thenReturnItemUpdated() {
        Boolean itemAvailableBeforeUpdate = item.isAvailable();
        Boolean itemAvailableAfterUpdateExpected = updateAvailableItemInputDto.getAvailable();

        ItemMapper.updateItemWithItemDto(item, updateAvailableItemInputDto);
        Boolean itemAvailableAfterUpdatedActual = item.isAvailable();
        assertEquals(itemAvailableAfterUpdateExpected, itemAvailableAfterUpdatedActual);
        assertNotEquals(itemAvailableBeforeUpdate, itemAvailableAfterUpdatedActual);
    }

    @Test
    void toItem() {
        Item expected = itemInput;
        Item actual = ItemMapper.toItem(itemInputDto, owner, request);
        assertEquals(expected, actual);
    }

    @Test
    void toItemDtoList() {
        List<ItemOutDto> expected = List.of(itemOutDto);
        List<ItemOutDto> actual = ItemMapper.toItemDtoList(PAGE_OF_ITEMS_1);
        assertEqualLists(expected, actual);
    }

    @Test
    void toItemResponseInRequest() {
        ItemInRequestDto expected = itemInRequestDto;
        ItemInRequestDto actual = ItemMapper.toItemResponseInRequest(item);
        assertEquals(expected, actual);
    }

    @Test
    void toItemResponseInRequestDtoList() {
        List<ItemInRequestDto> expected = List.of(itemInRequestDto);
        List<ItemInRequestDto> actual = ItemMapper.toItemResponseInRequestDtoList(List.of(item));
        assertEquals(expected, actual);
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