package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.UserEmailNotUniqueException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final String EXCEPTION_EMAIL_CONFLICT = "Email is already used.";
    private static final AtomicLong ID_PROVIDER = new AtomicLong(0);
    private final Map<Long, User> users = new HashMap<>();

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
            if (Objects.nonNull(userDto.getEmail()) && !userDto.getEmail().equals(user.getEmail())) {
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
        userOptional.ifPresent((user) -> {
            throw new UserEmailNotUniqueException(EXCEPTION_EMAIL_CONFLICT);
        });
    }
}
