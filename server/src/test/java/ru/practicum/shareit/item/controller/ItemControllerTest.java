package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInItemDto;
import ru.practicum.shareit.exception.AccessForbiddenException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemFullDto;
import ru.practicum.shareit.item.dto.ItemInputDto;
import ru.practicum.shareit.item.dto.ItemOutDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final Long ITEM_ID = 1L;
    private static final Long OWNER_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private static ItemInputDto itemInputDto;
    private static ItemOutDto itemOutDto;
    private static ItemFullDto itemFullDto;
    private static CommentInputDto commentInputDto;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemServiceImpl itemService;

    @BeforeEach
    void beforeEach() {
        itemInputDto = ItemInputDto.builder()
                .id(null)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(OWNER_ID)
                .requestId(null)
                .build();

        itemOutDto = ItemOutDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(OWNER_ID)
                .requestId(null)
                .build();

        itemFullDto = ItemFullDto.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .lastBooking(new BookingInItemDto())
                .nextBooking(new BookingInItemDto())
                .comments(List.of(new CommentFullDto()))
                .build();

        commentInputDto = CommentInputDto.builder()
                .text("comment text")
                .build();
    }

    @SneakyThrows
    @Test
    void create_whenUserExist_thenReturnOkAndItemOutDto() {
        when(itemService.create(itemInputDto, OWNER_ID)).thenReturn(itemOutDto);

        String actual = mockMvc.perform(post("/items")
                        .header(X_SHARER_USER_ID, OWNER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemInputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expected = objectMapper.writeValueAsString(itemOutDto);

        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void create_whenUserNotExist_thenReturnNotFound() {
        String messageExpected = "User not found.";
        when(itemService.create(itemInputDto, OWNER_ID))
                .thenThrow(new ShareItElementNotFoundException(messageExpected));

        mockMvc.perform(post("/items")
                        .header(X_SHARER_USER_ID, OWNER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemInputDto)))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertEquals(messageExpected,
                                Objects.requireNonNull(result.getResolvedException()).getMessage())
                );
    }

    @SneakyThrows
    @Test
    void getById_whenItemNotExist_thenReturnOkAndItemFullDto() {
        when(itemService.getById(OWNER_ID, ITEM_ID)).thenReturn(itemFullDto);

        String actual = mockMvc.perform(get("/items/{itemId}", ITEM_ID)
                        .header(X_SHARER_USER_ID, OWNER_ID)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expected = objectMapper.writeValueAsString(itemFullDto);
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void getById_whenItemNotExist_thenReturnNotFound() {
        String expectedMessage = "Item not found.";
        when(itemService.getById(OWNER_ID, ITEM_ID)).thenThrow(new ShareItElementNotFoundException(expectedMessage));

        mockMvc.perform(get("/items/{itemId}", ITEM_ID)
                        .header(X_SHARER_USER_ID, OWNER_ID)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertEquals(expectedMessage,
                                Objects.requireNonNull(result.getResolvedException()).getMessage())
                );
    }

    @SneakyThrows
    @Test
    void findAll_whenItemsExist_thenReturnOkAndListOfItems() {
        when(itemService.findAll(eq(OWNER_ID), anyInt(), anyInt())).thenReturn(List.of(itemFullDto));

        String actual = mockMvc.perform(get("/items")
                        .header(X_SHARER_USER_ID, OWNER_ID)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expected = objectMapper.writeValueAsString(List.of(itemFullDto));
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void update_whenUserExistItemExist_thenReturnOkAndItemOutDto() {
        when(itemService.update(itemInputDto, OWNER_ID, ITEM_ID)).thenReturn(itemOutDto);

        String actual = mockMvc.perform(patch("/items/{itemId}", ITEM_ID)
                        .header(X_SHARER_USER_ID, OWNER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemInputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expected = objectMapper.writeValueAsString(itemOutDto);
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void update_whenUserIsNotOwner_thenReturnForbidden() {
        String expectedMessage = "Only owner can change the item.";
        when(itemService.update(itemInputDto, USER_ID, ITEM_ID))
                .thenThrow(new AccessForbiddenException(expectedMessage));

        mockMvc.perform(patch("/items/{itemId}", ITEM_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemInputDto)))
                .andExpect(status().isForbidden())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof AccessForbiddenException))
                .andExpect(result ->
                        assertEquals(expectedMessage,
                                Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @SneakyThrows
    @Test
    void update_whenItemNotFound_thenReturnNotFound() {
        String expectedMessage = "Item not found";
        when(itemService.update(itemInputDto, USER_ID, ITEM_ID))
                .thenThrow(new ShareItElementNotFoundException(expectedMessage));

        mockMvc.perform(patch("/items/{itemId}", ITEM_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemInputDto)))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertEquals(expectedMessage,
                                Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @SneakyThrows
    @Test
    void search_whenGivenValidParams_themReturnOkAndListOfItemOutDtos() {

        when(itemService.search(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemOutDto));

        String actual = mockMvc.perform(get("/items/search")
                        .contentType("application/json")
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expected = objectMapper.writeValueAsString(List.of(itemOutDto));
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void search_whenGivenBlankText_themReturnOkAndEmptyList() {

        when(itemService.search(anyString(), anyInt(), anyInt())).thenReturn(List.of());

        String actual = mockMvc.perform(get("/items/search")
                        .contentType("application/json")
                        .param("text", Strings.EMPTY)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expected = objectMapper.writeValueAsString(List.of());
        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void addComment_whenValidRequest_thenReturnOkAndCommentFullDto() {
        CommentFullDto commentFullDto = new CommentFullDto();
        when(itemService.addComment(any(), anyLong(), anyLong())).thenReturn(commentFullDto);

        String actualResponse = mockMvc.perform(post("/items/{itemId}/comment", ITEM_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentInputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CommentFullDto actual = objectMapper.readValue(actualResponse, CommentFullDto.class);
        assertEquals(commentFullDto, actual);
    }
}