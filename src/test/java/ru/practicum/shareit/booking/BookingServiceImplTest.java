package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingFullDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ItemIsUnavailableException;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final LocalDateTime START = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime END = LocalDateTime.now().plusWeeks(2);

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private Map<BookingServiceImpl.SearchCondition, BiFunction<Long, Pageable, Page<Booking>>> conditions;
    private BookingService bookingService;
    private BookingInputDto bookingInputDto;
    private User user1;
    private User user2;
    private Item item;
    private Booking booking;

    @BeforeEach
    void beforeEach() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository, conditions);
        bookingInputDto = BookingInputDto.builder()
                .id(null)
                .start(START)
                .end(END)
                .itemId(1L)
                .status(null)
                .build();

        user1 = User.builder()
                .id(1L)
                .name("userName")
                .email("user@email.ru")
                .build();

        user2 = User.builder()
                .id(2L)
                .name("userName2")
                .email("user2@email.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(user2)
                .request(null)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(START)
                .end(END)
                .item(item)
                .booker(user1)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void create_whenUserExistItemAvailableAuthorIsNotOwner_thenReturnBookingFullDto() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(bookingInputDto.getItemId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingFullDto actual = bookingService.create(bookingInputDto, user1.getId());
        BookingFullDto expected = BookingMapper.toBookingFullDto(booking);

        assertThat(expected, samePropertyValuesAs(actual));
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(userRepository, times(1)).findById(user1.getId());
        verify(itemRepository, times(2)).findById(item.getId());

    }

    @Test
    void create_whenUserNotExistItemAvailableAuthorIsNotOwner_thenThrowNotFound() {
        doThrow(new ShareItElementNotFoundException("User not found.")).when(userRepository).findById(user1.getId());

        assertThrows(ShareItElementNotFoundException.class, () -> bookingService.create(bookingInputDto, user1.getId()));

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(itemRepository, never()).findById(item.getId());

    }

    @Test
    void create_whenUserExistItemNotAvailableAuthorIsNotOwner_thenThrowNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(bookingInputDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(ItemIsUnavailableException.class, () -> bookingService.create(bookingInputDto, user1.getId()));

        verify(userRepository, times(1)).findById(user1.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_whenUserExistItemIsAvailableAuthorIsOwner_thenThrowNotFound() {
        item.setOwner(user1);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(bookingInputDto.getItemId())).thenReturn(Optional.of(item));

        assertThrows(ShareItElementNotFoundException.class, () -> bookingService.create(bookingInputDto, user1.getId()));

        verify(userRepository, times(1)).findById(user1.getId());
        verify(itemRepository, times(2)).findById(item.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }



    @Test
    void getById() {
    }

    @Test
    void findBookings() {
    }

    @Test
    void setStatus() {
    }
}