package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> getById(Long userId);

    List<User> findAll();

    User create(UserDto userDto);

    Optional<User> update(UserDto userDto, Long userId);
    void delete(Long userId);
}
