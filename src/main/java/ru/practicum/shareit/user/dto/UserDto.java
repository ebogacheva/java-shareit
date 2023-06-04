package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.user.constraint_groups.CreateUserDtoValidation;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UserDto {

    private Long id;
    @NotNull(groups = CreateUserDtoValidation.class)
    private String name;
    @Email
    @NotNull(groups = CreateUserDtoValidation.class)
    private String email;
}
