package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.dto.ItemInRequestDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final Long REQUEST_ID = 1L;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final Long USER_ID = 1L;
    private static final LocalDateTime CREATED = LocalDateTime.now();

    private static ItemRequestInputDto itemRequestInputDto;
    private static RequestWithItemsDto requestWithItemsDto;
    private static RequestWithItemsDto requestCreatedDto;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @BeforeEach
    void beforeEach() {
        itemRequestInputDto = ItemRequestInputDto.builder()
                .description("request description")
                .build();

        requestWithItemsDto = RequestWithItemsDto.builder()
                .id(REQUEST_ID)
                .description("request description")
                .requester(USER_ID)
                .created(CREATED)
                .items(List.of(new ItemInRequestDto()))
                .build();

        requestCreatedDto = RequestWithItemsDto.builder()
                .id(REQUEST_ID)
                .description("request description")
                .requester(USER_ID)
                .created(CREATED)
                .items(null)
                .build();
    }

    @SneakyThrows
    @Test
    void create_whenValidInput_thenReturnOkAndItemRequestDto() {
        when(itemRequestService.create(any(), anyLong())).thenReturn(requestCreatedDto);

        String actual = mockMvc.perform(post("/requests")
                .header(X_SHARER_USER_ID, USER_ID)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(itemRequestInputDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expected = objectMapper.writeValueAsString(requestCreatedDto);

        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void create_whenInvalidInput_thenReturnBadRequest() {
        itemRequestInputDto.setDescription(null);

        mockMvc.perform(post("/requests")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(
                                result.getResolvedException()).getMessage().contains("must not be blank")));
    }

    @SneakyThrows
    @Test
    void create_whenUserNotExist_thenReturnBadRequest() {
        when(itemRequestService.create(any(), anyLong()))
                .thenThrow(new ShareItElementNotFoundException("User not found."));

        mockMvc.perform(post("/requests")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestInputDto)))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(
                                result.getResolvedException()).getMessage().contains("User not found.")));
    }


    @SneakyThrows
    @Test
    void findAll_thenReturnOkAndListOfItemRequestDtos() {
        when(itemRequestService.findAll(anyLong())).thenReturn(List.of(requestWithItemsDto));

        String actualInString = mockMvc.perform(get("/requests")
                .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expected = objectMapper.writeValueAsString(List.of(requestWithItemsDto));

        assertEquals(expected, actualInString);
    }

    @SneakyThrows
    @Test
    void testFindAll_thenReturnOkAndListOfDtos() {
        when(itemRequestService.findAll(anyLong(), anyInt(), anyInt())).thenReturn(List.of(requestWithItemsDto));

        String actualInString = mockMvc.perform(get("/requests/all")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("from", "0")
                        .param("size","1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expected = objectMapper.writeValueAsString(List.of(requestWithItemsDto));

        assertEquals(expected, actualInString);
    }

    @SneakyThrows
    @Test
    void testFindAll_whenUserNotExists_thenReturnNotFound() {
        when(itemRequestService.findAll(anyLong(), anyInt(), anyInt()))
                .thenThrow(new ShareItElementNotFoundException("User not found"));

        mockMvc.perform(get("/requests/all")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("from", "0")
                        .param("size","1"))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(
                                result.getResolvedException()).getMessage().contains("User not found")));
    }

    @Test
    void getById_whenRequestExist_thenReturnOkAndRequestWithItemsDto() throws Exception {
        when(itemRequestService.getById(anyLong(), anyLong())).thenReturn(requestWithItemsDto);

        String actual = mockMvc.perform(get("/requests/{requestId}", REQUEST_ID)
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expected = objectMapper.writeValueAsString(requestWithItemsDto);

        assertEquals(expected, actual);
    }

    @Test
    void getById_whenUserNotExist_thenReturnNotFound() throws Exception {
        when(itemRequestService.getById(anyLong(), anyLong())).thenThrow(new ShareItElementNotFoundException("User not found."));

        mockMvc.perform(get("/requests/{requestId}", REQUEST_ID)
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(
                                result.getResolvedException()).getMessage().contains("User not found")));
    }

    @Test
    void getById_whenRequestNotExist_thenReturnNotFound() throws Exception {
        when(itemRequestService.getById(anyLong(), anyLong())).thenThrow(new ShareItElementNotFoundException("Request not found."));

        mockMvc.perform(get("/requests/{requestId}", REQUEST_ID)
                        .header(X_SHARER_USER_ID, USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(
                                result.getResolvedException()).getMessage().contains("Request not found.")));
    }
}