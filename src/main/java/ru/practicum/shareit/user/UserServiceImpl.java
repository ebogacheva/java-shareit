package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepositoryImpl;

    public User create(UserDto userDto) {
       return userRepositoryImpl.create(userDto);
    }

    public User getById(Long userId) {
        Optional<User> userOptional = userRepositoryImpl.getById(userId);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else throw new ShareItElementNotFoundException("User not found.");
    }

    @Override
    public List<User> findAll() {
        return userRepositoryImpl.findAll();
    }

    public User update(UserDto userDto, Long userId) {
        Optional<User> userOptional = userRepositoryImpl.update(userDto, userId);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else throw new ShareItElementNotFoundException("User not found.");
    }

    @Override
    public void delete(Long userId) {
        userRepositoryImpl.delete(userId);
    }
}
