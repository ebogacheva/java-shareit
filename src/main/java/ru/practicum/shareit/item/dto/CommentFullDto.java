package ru.practicum.shareit.item.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class CommentFullDto {

    private Long id;
    private String text;
    private Long itemId;
    private String authorName;
    private LocalDateTime created;
}