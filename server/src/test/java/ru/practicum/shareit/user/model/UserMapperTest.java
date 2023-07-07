package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperTest {

    private static final Long USER_ID = 1L;

    private static User user;
    private static UserDto userDto;
    private static UserDto updatedNameUserDto;
    private static UserDto updatedEmailUserDto;
    private static UserDto updatedNameAndEmailUserDto;

    @BeforeEach
    void beforeEach() {

        user = User.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();

        userDto = UserDto.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();

        updatedNameUserDto = UserDto.builder()
                .name("updatedName")
                .build();

        updatedEmailUserDto = UserDto.builder()
                .email("updated@email.ru")
                .build();

        updatedNameAndEmailUserDto = UserDto.builder()
                .name("updatedName")
                .email("updated@email.ru")
                .build();
    }

    @Test
    void toUserDto_thenReturnUserDto() {
        UserDto expected = userDto;
        UserDto actual = UserMapper.toUserDto(user);
        assertEquals(expected, actual);
    }

    @Test
    void updateUserWithUserDto_whenUpdateUserName_thenUpdatedUser() {
        User expected = User.builder()
                .id(USER_ID)
                .name("updatedName")
                .email("user@email.ru")
                .build();
        UserMapper.updateUserWithUserDto(user, updatedNameUserDto);
        assertEquals(expected, user);
    }

    @Test
    void updateUserWithUserDto_whenUpdateUserNameAndEmail_thenUpdatedUser() {
        User expected = User.builder()
                .id(USER_ID)
                .name("updatedName")
                .email("updated@email.ru")
                .build();
        UserMapper.updateUserWithUserDto(user, updatedNameAndEmailUserDto);
        assertEquals(expected, user);
    }

    @Test
    void updateUserWithUserDto_whenUpdateEmail_thenUpdatedUser() {
        User expected = User.builder()
                .id(USER_ID)
                .name("userName")
                .email("updated@email.ru")
                .build();
        UserMapper.updateUserWithUserDto(user, updatedEmailUserDto);
        assertEquals(expected, user);
    }

    @Test
    void toUser_thenReturnUser() {
        User expected = user;
        User actual = UserMapper.toUser(userDto, USER_ID);
        assertEquals(expected, actual);
    }

    @Test
    void toUserDtoList() {
        List<UserDto> expected = List.of(userDto);
        List<UserDto> actual = UserMapper.toUserDtoList(List.of(user));
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