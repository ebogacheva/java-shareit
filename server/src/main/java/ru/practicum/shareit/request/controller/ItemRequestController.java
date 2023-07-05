package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestServiceImpl requestService;

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public RequestWithItemsDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                      @RequestBody ItemRequestInputDto itemRequestInputDto) {
        return requestService.create(itemRequestInputDto, userId);
    }

    @GetMapping
    public List<RequestWithItemsDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        return requestService.findAll(userId);
    }

    @GetMapping(value = "/all")
    public List<RequestWithItemsDto> findAll(@RequestHeader(X_SHARER_USER_ID) long userId,
                                             @RequestParam(required = false, defaultValue = "0") int from,
                                             @RequestParam(required = false, defaultValue = "10") int size) {
        return requestService.findAll(userId, from, size);
    }

    @GetMapping(value = "/{requestId}")
    public RequestWithItemsDto getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                       @PathVariable Long requestId) {
        return requestService.getById(userId, requestId);
    }

}
