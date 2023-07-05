package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                 @Valid @RequestBody ItemRequestInputDto itemRequestInputDto) {
        return itemRequestClient.create(itemRequestInputDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader(X_SHARER_USER_ID) long userId) {
        return itemRequestClient.findAll(userId);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<Object> findAll(@RequestHeader(X_SHARER_USER_ID) long userId,
                                             @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                             @Min(1) @RequestParam(required = false, defaultValue = "10") int size) {
        return itemRequestClient.findAll(userId, from, size);
    }

    @GetMapping(value = "/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                       @PathVariable Long requestId) {
        return itemRequestClient.getById(userId, requestId);
    }

}
