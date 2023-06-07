package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.exception.UserEmailNotUniqueException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String EXCEPTION_NOT_FOUND_INFO = "User not found.";
    private static final String EXCEPTION_EMAIL_NOT_UNIQUE = "User email already exists.";

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        User userFromDto = UserMapper.toUser(userDto, null);
        if (findUserByEmail(userDto.getEmail())) {
            throw new UserEmailNotUniqueException(EXCEPTION_EMAIL_NOT_UNIQUE);
        }
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
    public UserDto update(UserDto userDto, Long userId) {
        User user = findById(userId);
        UserMapper.updateUserWithUserDto(user, userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public boolean existsById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ShareItElementNotFoundException(EXCEPTION_NOT_FOUND_INFO);
        } return true;
    }

    private User findById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_NOT_FOUND_INFO));
    }

    private boolean findUserByEmail(String email) {
        return (userRepository.findUserByEmail(email) != null);
    }

}
