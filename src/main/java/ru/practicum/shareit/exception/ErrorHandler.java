package ru.practicum.shareit.exception;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.user.UserController;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {ItemController.class, UserController.class, ItemRequestController.class,
        BookingController.class, MethodArgumentNotValidException.class})
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(final MethodArgumentNotValidException e) {
        MethodParameter parameter = e.getParameter();
        return Map.of("Validation error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleElementNotFound(final ShareItElementNotFoundException e) {
        return Map.of("Not found error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleUserNotAuthorizedCorrectly(final AccessForbiddenException e) {
        return Map.of("Access error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleItemIsNotAvailable(final ItemIsUnavailableException e) {
        return Map.of("Item access error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnsupportedStatus(final UnsupportedStatusException e) {
        return new ErrorResponse("Unknown state: " + e.getMessage(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBookingIsAlreadyApproved(final BookingIsAlreadyApprovedException e) {
        return new ErrorResponse("Request error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNoUserBookingAvailableToComment(final NoUserBookingAvailableToComment e) {
        return new ErrorResponse("Request error: ", e.getMessage());
    }

}
