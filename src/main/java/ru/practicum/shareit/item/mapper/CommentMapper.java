package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {

    public static Comment toComment(CommentInputDto commentDto, Item item, User user) {
        return Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(user)
                .build();
    }

    public static CommentFullDto toCommentResponseDto(Comment comment) {
        return CommentFullDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .itemId(comment.getItem().getId())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static List<CommentFullDto> toCommentDtoList(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentResponseDto).collect(Collectors.toList());
    }
}
