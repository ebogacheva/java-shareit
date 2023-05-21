package ru.practicum.shareit.user.model;


import ru.practicum.shareit.user.dto.UserDto;

import java.util.Objects;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getName(),
                user.getEmail()
        );
    }

    public static void updateUserWithUserDto(User user, UserDto userDto) {
        if (Objects.nonNull(userDto.getName())) {
            user.setName(userDto.getName());
        }
        if (Objects.nonNull(userDto.getEmail())) {
            user.setEmail(userDto.getEmail());
        }
    }

    public static User toUser(UserDto userDto, Long id) {
        return User.builder()
                .id(id)
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
