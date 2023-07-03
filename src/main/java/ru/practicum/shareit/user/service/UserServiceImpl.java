package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String EXCEPTION_NOT_FOUND_INFO = "User not found.";

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        User userFromDto = UserMapper.toUser(userDto, null);
        User created = userRepository.save(userFromDto);
        return UserMapper.toUserDto(created);
    }

    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toUserDto(findById(userId));
    }

    @Override
    public List<UserDto> findAll() {
        return UserMapper.toUserDtoList(userRepository.findAll());
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {
        User user = findById(userId);
        UserMapper.updateUserWithUserDto(user, userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }


    private User findById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_NOT_FOUND_INFO));
    }
}
