package ru.practicum.shareit.user;

import ru.practicum.shareit.exception.UserEmailNotUniqueException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class UserRepositoryImpl implements UserRepository{

    private static final AtomicLong ID_PROVIDER = new AtomicLong(0);
    private final HashMap<Long, User> users = new HashMap<>();

    @Override
    public Optional<User> getById(Long userId) {
        User user = users.get(userId);
        if (Objects.nonNull(user)) {
            return Optional.of(user);
        } else return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(UserDto userDto) {
        checkUserEmailIsUnique(userDto.getEmail());
        Long id = ID_PROVIDER.incrementAndGet();
        User created = UserMapper.toUser(userDto, id);
        users.put(id, created);
        return created;
    }

    @Override
    public Optional<User> update(UserDto userDto, Long userId) {
        User user = users.get(userId);
        if (Objects.nonNull(user) && Objects.nonNull(userDto)) {
            if (Objects.nonNull(userDto.getEmail())) {
                checkUserEmailIsUnique(userDto.getEmail());
            }
            UserMapper.updateUserWithUserDto(user, userDto);
            users.put(userId, user);
        }
        return getById(userId);
    }

    @Override
    public void delete(Long userId) {
        users.remove(userId);
    }

    private void checkUserEmailIsUnique(String email) {
        Optional<User> userOptional =
                users.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
        if (userOptional.isPresent()) {
            throw new UserEmailNotUniqueException("Email " + email + " is already used.");
        }
    }
}
