package ru.practicum.shareit.booking.dto.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BookingStartEndValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BookingStartEndValidation {
    String message() default "Invalid booking's start-end info.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
