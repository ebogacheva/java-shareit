package ru.practicum.shareit.exception;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.item.controller.ItemController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.user.controller.UserController;

@RestControllerAdvice(assignableTypes = {ItemController.class, UserController.class, ItemRequestController.class,
        BookingController.class, ItemRequestController.class, MethodArgumentNotValidException.class})
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final ShareItElementNotFoundException e) {
        return new ErrorResponse("Not found error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handle(final AccessForbiddenException e) {
        return new ErrorResponse("Access error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final ItemIsUnavailableException e) {
        return new ErrorResponse("Item access error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final UnsupportedStatusException e) {
        return new ErrorResponse("Unknown state: " + e.getMessage(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final BookingIsAlreadyApprovedException e) {
        return new ErrorResponse("Request error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final NoUserBookingAvailableToComment e) {
        return new ErrorResponse("Request error: ", e.getMessage());
    }

}
