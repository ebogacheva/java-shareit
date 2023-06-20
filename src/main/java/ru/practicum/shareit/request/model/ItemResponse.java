package ru.practicum.shareit.request.model;

import lombok.*;

import javax.persistence.Entity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ItemResponse {

    private Long itemId;
    private String itemName;
    private Long userId;
}
