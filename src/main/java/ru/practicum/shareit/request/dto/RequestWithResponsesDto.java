package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.request.model.ItemResponse;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RequestWithResponsesDto {

    private Long id;
    private String description;
    private Long requester;
    private LocalDateTime created;
    private List<ItemResponse> responses;

}
