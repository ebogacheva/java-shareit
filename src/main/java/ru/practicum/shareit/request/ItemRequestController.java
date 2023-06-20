package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.dto.RequestWithResponsesDto;
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

    private final ItemRequestService itemRequestServiceImpl;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestFullDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestServiceImpl.create(itemRequestDto, userId);
    }

    @GetMapping
    public List<RequestWithResponsesDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        return itemRequestServiceImpl.findAll(userId);
    }

    @GetMapping(value = "/all")
    public List<RequestWithResponsesDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId,
                                            @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                            @Min(0) @RequestParam(required = false, defaultValue = "10") int size) {
        return itemRequestServiceImpl.findAll(userId, from, size);
    }

    @GetMapping(value = "/{requestId}")
    public RequestWithResponsesDto getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                           @PathVariable Long requestId) {
        return itemRequestServiceImpl.getById(userId, requestId);
    }

}
