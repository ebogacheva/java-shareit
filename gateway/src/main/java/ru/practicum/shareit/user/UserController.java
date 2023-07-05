package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private static final int MIN_ID_VALUE = 1;

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        return userClient.create(userDto);
    }

    @GetMapping(value = "/{userId}")
    public ResponseEntity<Object> getById(@NotNull @Min(MIN_ID_VALUE) @PathVariable Long userId) {
        return userClient.getById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        return userClient.findAll();
    }

    @PatchMapping(value = "/{userId}")
    public ResponseEntity<Object> update(@NotNull @Min(MIN_ID_VALUE) @PathVariable Long userId,
                          @RequestBody UserDto userDto) {
        return userClient.update(userDto, userId);
    }

    @DeleteMapping(value = "/{userId}")
    public void delete(@NotNull @Min(MIN_ID_VALUE) @PathVariable Long userId) {
        userClient.delete(userId);
    }
}
