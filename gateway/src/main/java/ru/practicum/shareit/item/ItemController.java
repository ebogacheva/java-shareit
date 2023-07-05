package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemInputDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                         @Valid @RequestBody ItemInputDto itemInputDto) {
        return itemClient.create(itemInputDto, userId);
    }

    @GetMapping(value = "/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                          @PathVariable Long itemId) {
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader(X_SHARER_USER_ID) long userId,
                                          @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                          @Min(1) @RequestParam(required = false, defaultValue = "10") int size) {
        return itemClient.findAll(userId, from, size);
    }

    @PatchMapping(value = "/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(X_SHARER_USER_ID) long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemInputDto itemInputDto) {
        return itemClient.update(itemInputDto, userId, itemId);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Object> search(@RequestHeader(X_SHARER_USER_ID) long userId,
                                         @RequestParam(value = "text", defaultValue = "", required = false) String searchBy,
                                         @Min(0) @RequestParam(required = false, defaultValue = "0") int from,
                                         @Min(1) @RequestParam(required = false, defaultValue = "10") int size) {
        return itemClient.search(searchBy, userId, from, size);
    }

    @PostMapping(value = "/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentInputDto comment) {
        return itemClient.addComment(comment, itemId, userId);
    }

}
