package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final LocalDateTime START = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime END = LocalDateTime.now().plusWeeks(2);
    private static final Long BOOKING_ID_1 = 1L;
    private static final Long BOOKING_ID_2 = 2L;
    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long OTHER_ID = 3L;
    private static final Long ITEM_ID = 1L;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final Long INVALID_BOOKING_ID = -10L;


    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BookingService bookingService;

    private BookingInputDto bookingInputDto;
    private User user;
    private User owner;
    private User other;
    private Item item;
    private BookingFullDto bookingFullDto1;
    private BookingFullDto bookingFullDto2;


    @BeforeEach
    void beforeEach() {
        bookingInputDto = BookingInputDto.builder()
                .id(null)
                .start(START)
                .end(END)
                .itemId(ITEM_ID)
                .status(null)
                .build();

        user = User.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();

        other = User.builder()
                .id(OTHER_ID)
                .name("otherName")
                .email("otherName@email.ru")
                .build();

        owner = User.builder()
                .id(OWNER_ID)
                .name("ownerName")
                .email("owner@email.ru")
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        bookingFullDto1 = BookingFullDto.builder()
                .id(BOOKING_ID_1)
                .start(START)
                .end(END)
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();


        bookingFullDto2 = BookingFullDto.builder()
                .id(BOOKING_ID_2)
                .start(START)
                .end(END)
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
    }

    @SneakyThrows
    @Test
    void create_whenValidInputDto_thenReturnOkStatusAndBookingFullDto() {
        when(bookingService.create(bookingInputDto, USER_ID)).thenReturn(bookingFullDto1);

        String actual = mockMvc.perform(post("/bookings")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String expected = objectMapper.writeValueAsString(bookingFullDto1);

        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    void create_whenInvalidInputBookingDtoStartAfterEnd_thenBadRequest() {

        bookingInputDto.setStart(END);
        bookingInputDto.setEnd(START);

        mockMvc.perform(post("/bookings")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(result ->
                        assertThat(result.getResponse().getContentAsString(),
                                containsString("Invalid booking's start-end info.")));
    }

    @SneakyThrows
    @Test
    void update_whenInputValid_thenReturnOkStatusAndBookingFullDto() {
        bookingFullDto1.setStatus(BookingStatus.APPROVED);
        when(bookingService.setStatus(USER_ID, BOOKING_ID_1, true)).thenReturn(bookingFullDto1);

        String actualInString = mockMvc.perform(patch("/bookings/{bookingId}", BOOKING_ID_1)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("approved", "true")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BookingFullDto actual = objectMapper.readValue(actualInString, BookingFullDto.class);

        assertThat(actual,samePropertyValuesAs(bookingFullDto1));
    }

    @SneakyThrows
    @Test
    void update_whenInvalidInputNegativeBookingId_thenBadRequest() {

        mockMvc.perform(patch("/bookings/{bookingId}", INVALID_BOOKING_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("approved", "true")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result ->
                        assertThat(result.getResponse().getContentAsString(),
                                containsString("must be greater than or equal to 0")));
    }

    @SneakyThrows
    @Test
    void update_whenInvalidInputNotSpecifiedApproved_thenBadRequest() {
        mockMvc.perform(patch("/bookings/{bookingId}", BOOKING_ID_1)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertNotNull(result.getResolvedException()));
    }

    @SneakyThrows
    @Test
    void getById_whenInputValid_thenOkAndReturnBookingFullDto() {
        when(bookingService.getById(USER_ID, BOOKING_ID_1)).thenReturn(bookingFullDto1);

        String actualInString = mockMvc.perform(get("/bookings/{bookingId}", BOOKING_ID_1)
                .header(X_SHARER_USER_ID, USER_ID)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BookingFullDto actual = objectMapper.readValue(actualInString, BookingFullDto.class);
        assertThat(actual,samePropertyValuesAs(bookingFullDto1));
    }

    @SneakyThrows
    @Test
    void getById_whenInvalidInputNegativeBookingId_thenBadRequest() {

        mockMvc.perform(get("/bookings/{bookingId}", INVALID_BOOKING_ID)
                        .header(X_SHARER_USER_ID, USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ConstraintViolationException))
                .andExpect(result ->
                        assertThat(result.getResponse().getContentAsString(),
                                containsString("must be greater than or equal to 0")));
    }

    @SneakyThrows
    @Test
    void findBookingsForBooker_whenValidInputAllParametersGiven_thenOkReturnListOfDtos() {
        when(bookingService.findBookings(anyLong(), anyString(), anyString(), anyInt(), anyInt()))
                    .thenReturn(List.of(bookingFullDto1));

        String actualString = mockMvc.perform(get("/bookings")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size","1")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<BookingFullDto> actual = objectMapper.readValue(actualString, new TypeReference<>() {});
        List<BookingFullDto> expected = List.of(bookingFullDto1);

        assertTrue(expected.size() == actual.size()
                && expected.containsAll(actual)
                && actual.containsAll(expected));
    }

    @SneakyThrows
    @Test
    void findBookingsForBooker_whenValidInputStateNotGiven_thenOkReturnListOfDtos() {
        when(bookingService.findBookings(anyLong(), any(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingFullDto1));

        String actualString = mockMvc.perform(get("/bookings")
                        .header(X_SHARER_USER_ID, USER_ID)
                        .param("from", "0")
                        .param("size","1")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<BookingFullDto> actual = objectMapper.readValue(actualString, new TypeReference<>() {});
        List<BookingFullDto> expected = List.of(bookingFullDto1);

        assertTrue(expected.size() == actual.size()
                && expected.containsAll(actual)
                && actual.containsAll(expected));
    }

    @SneakyThrows
    @Test
    void findBookingsForOwner_whenValidInputAllParametersGiven_thenOkReturnListOfDtos() {
        when(bookingService.findBookings(anyLong(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingFullDto1));

        String actualString = mockMvc.perform(get("/bookings/owner")
                        .header(X_SHARER_USER_ID, OWNER_ID)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size","1")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<BookingFullDto> actual = objectMapper.readValue(actualString, new TypeReference<>() {});
        List<BookingFullDto> expected = List.of(bookingFullDto1);

        assertTrue(expected.size() == actual.size()
                && expected.containsAll(actual)
                && actual.containsAll(expected));
    }
}