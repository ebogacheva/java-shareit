package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    UserDto create(UserDto userDto);

    UserDto getById(Long userId);
    List<UserDto> findAll();

    UserDto update(UserDto userDto, Long userId);

    void delete(Long userId);

    boolean existsById(Long userId);

}
