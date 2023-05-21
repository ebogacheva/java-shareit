package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    User create(UserDto userDto);
    User getById(Long userId);
    List<User> findAll();
    User update(UserDto userDto, Long userId);
    void delete(Long userId);

}
