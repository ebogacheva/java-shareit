package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService requestService;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public RequestWithItemsDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @Valid @RequestBody ItemRequestInputDto itemRequestInputDto) {
        return requestService.create(itemRequestInputDto, userId);
    }

    @GetMapping
    public List<RequestWithItemsDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        return requestService.findAll(userId);
    }

    @GetMapping(value = "/all")
    public List<RequestWithItemsDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId,
                                             @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                             @Min(0) @RequestParam(required = false, defaultValue = "10") int size) {
        return requestService.findAll(userId, from, size);
    }

    @GetMapping(value = "/{requestId}")
    public RequestWithItemsDto getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                       @PathVariable Long requestId) {
        return requestService.getById(userId, requestId);
    }

}
