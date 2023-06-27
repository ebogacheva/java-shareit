package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentFullDto;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private static final Long COMMENT_ID = 1L;
    private static final Long ITEM_ID = 1L;
    private static final Long AUTHOR_ID = 1L;
    private static final LocalDateTime CREATED = LocalDateTime.now();

    private static Comment commentBeforeDb;
    private static Comment commentFromDb;
    private static CommentFullDto commentFullDto;
    private static CommentInputDto commentInputDto;
    private static Item item;
    private static User author;

    @BeforeAll
    static void beforeAll() {

        author = User.builder()
                .id(AUTHOR_ID)
                .name("authorName")
                .email("author@email.ru")
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(new User())
                .request(null)
                .build();

        commentBeforeDb = Comment.builder()
                .id(null)
                .text("comment text")
                .item(item)
                .author(author)
                .created(null)
                .build();

        commentFromDb = Comment.builder()
                .id(COMMENT_ID)
                .text("comment text")
                .item(item)
                .author(author)
                .created(CREATED)
                .build();

        commentInputDto = CommentInputDto.builder()
                .text("comment text")
                .build();

        commentFullDto = CommentFullDto.builder()
                .id(COMMENT_ID)
                .text("comment text")
                .itemId(ITEM_ID)
                .authorName("authorName")
                .created(CREATED)
                .build();
    }

    @Test
    void toComment() {
        Comment expected = commentBeforeDb;
        Comment actual = CommentMapper.toComment(commentInputDto, item, author);
        assertEquals(expected, actual);
    }

    @Test
    void toCommentResponseDto() {
        CommentFullDto expected = commentFullDto;
        CommentFullDto actual = CommentMapper.toCommentFullDto(commentFromDb);
        assertEquals(expected, actual);
    }

    @Test
    void toCommentDtoList() {
        List<CommentFullDto> expected = List.of(commentFullDto);
        List<CommentFullDto> actual = CommentMapper.toCommentDtoList(List.of(commentFromDb));
        assertEquals(expected, actual);
    }
}