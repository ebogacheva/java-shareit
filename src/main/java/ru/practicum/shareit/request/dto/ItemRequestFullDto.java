package ru.practicum.shareit.request.dto;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestFullDto {
    private Long id;
    private String description;
    private Long requester;
    @CreationTimestamp
    private LocalDateTime created;
}
