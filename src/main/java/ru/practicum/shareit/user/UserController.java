package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.constraint_groups.CreateUserDtoValidation;
import ru.practicum.shareit.user.constraint_groups.UpdateUserDtoValidation;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private static final int MIN_ID_VALUE = 1;
    private final UserServiceImpl userService;

    @PostMapping
    public UserDto create(@Validated(CreateUserDtoValidation.class)
                              @RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @GetMapping(value = "/{userId}")
    public UserDto getById(@NotNull @Min(MIN_ID_VALUE) @PathVariable Long userId) {
        return userService.getById(userId);
    }

    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @PatchMapping(value = "/{userId}")
    public UserDto update(@NotNull @Min(MIN_ID_VALUE) @PathVariable Long userId,
                          @Validated(UpdateUserDtoValidation.class) @RequestBody UserDto userDto) {
        return userService.update(userDto, userId);
    }

    @DeleteMapping(value = "/{userId}")
    public void delete(@NotNull @Min(MIN_ID_VALUE) @PathVariable Long userId) {
        userService.delete(userId);
    }
}
