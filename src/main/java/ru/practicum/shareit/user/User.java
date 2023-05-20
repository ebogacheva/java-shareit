package ru.practicum.shareit.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;

/**
 * TODO Sprint add-controllers.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    private Long id;
    private String name;
    @Email
    @EqualsAndHashCode.Include
    private String email;
}
