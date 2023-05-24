package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String EXCEPTION_NOT_FOUND_INFO = "User not found.";

    private final UserRepository userRepositoryImpl;

    public User create(UserDto userDto) {
       return userRepositoryImpl.create(userDto);
    }

    public User getById(Long userId) {
        Optional<User> userOptional = userRepositoryImpl.getById(userId);
        return userOptional.orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_NOT_FOUND_INFO));
    }

    @Override
    public List<User> findAll() {
        return userRepositoryImpl.findAll();
    }

    public User update(UserDto userDto, Long userId) {
        Optional<User> userOptional = userRepositoryImpl.update(userDto, userId);
        return userOptional.orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_NOT_FOUND_INFO));
    }

    @Override
    public void delete(Long userId) {
        userRepositoryImpl.delete(userId);
    }
}
