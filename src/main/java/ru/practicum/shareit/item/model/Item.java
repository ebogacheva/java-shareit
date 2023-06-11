package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Setter
@Entity(name = "items")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotNull
    @NotBlank
    @Column(name = "name")
    private String name;
    @NotNull
    @NotBlank
    @Column(name = "description")
    private String description;
    @NotNull
    @Column(name = "is_available")
    private boolean available;
    @NotNull
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "owner_id")
    private User owner;
    @ManyToOne(targetEntity = ItemRequest.class)
    @JoinColumn(name = "request_id")
    private ItemRequest request;
}
