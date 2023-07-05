package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;


import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userInputDto;
    private UserDto userOutDto;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();

        userInputDto = UserDto.builder()
                .id(null)
                .name("userName")
                .email("user@email.ru")
                .build();

        userOutDto = UserDto.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();
    }

    @Test
    void create_thenReturnUserDto() {
        when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(user);
        UserDto expected = userOutDto;

        UserDto actual = userService.create(userInputDto);

        assertThat(actual, samePropertyValuesAs(expected));
        verify(userRepository, times(1)).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void getById_whenUserExist_thenReturnUserDto() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        UserDto expected = userOutDto;

        UserDto actual = userService.getById(USER_ID);
        assertThat(actual, samePropertyValuesAs(expected));
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void getById_whenUserNotExist_thenThrowNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        String expectedMessage = "User not found.";

        Exception actual = assertThrows(ShareItElementNotFoundException.class,
                () -> userService.getById(USER_ID));
        assertEquals(expectedMessage, actual.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void findAll_whenUsersExist_thenReturnListOfDtos() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserDto> expected = List.of(userOutDto);

        List<UserDto> actual = userService.findAll();

        assertEqualLists(expected, actual);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void update_whenUserExist_thenReturnUserDtoUpdated() {
        user.setName("updated");
        userInputDto.setName("updated");
        userOutDto.setName("updated");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto expected = userOutDto;
        UserDto actual = userService.update(userInputDto, USER_ID);

        assertThat(actual, samePropertyValuesAs(expected));
        assertThat(actual).hasFieldOrPropertyWithValue("name", "updated");
        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void update_whenUserNotExist_thenThrowNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        String expectedMessage = "User not found.";

        Exception actual = assertThrows(ShareItElementNotFoundException.class,
                () -> userService.getById(USER_ID));
        assertEquals(expectedMessage, actual.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, never()).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void delete() {
        userService.delete(USER_ID);
        verify(userRepository, times(1)).deleteById(USER_ID);
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