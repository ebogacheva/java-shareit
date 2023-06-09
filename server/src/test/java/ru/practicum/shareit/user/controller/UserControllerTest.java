package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    private static UserDto userInputDto;
    private static UserDto userOutDto;

    @BeforeEach
    void beforeEach() {
        userInputDto = UserDto.builder()
                .id(null)
                .name("userName")
                .email("user@email.ru")
                .build();

        userOutDto = UserDto.builder()
                .id(USER_ID)
                .name("userName")
                .email("user@email.ru")
                .build();
    }

    @Test
    void create_whenValidInput_thenReturnOkAndUserOutDto() throws Exception {
        when(userService.create(userInputDto)).thenReturn(userOutDto);

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userInputDto)))
                .andExpect(status().isOk())
                .andReturn();
        String expected = objectMapper.writeValueAsString(userOutDto);
        String actual = result.getResponse().getContentAsString();

        assertEquals(expected, actual);
        assertThat(userOutDto, samePropertyValuesAs(userInputDto, "id"));
    }

    @Test
    void getById_whenUserExist_thenReturnOkAndUserDto() throws Exception {
        when(userService.getById(USER_ID)).thenReturn(userOutDto);

        String actualInString = mockMvc.perform(get("/users/{userId}", USER_ID)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto expected = userOutDto;
        UserDto actual = objectMapper.readValue(actualInString, UserDto.class);
        assertEquals(expected, actual);
    }

    @Test
    void getById_whenUserNotExist_thenNotFound() throws Exception {
        when(userService.getById(USER_ID)).thenThrow(new ShareItElementNotFoundException("User not found."));
        mockMvc.perform(get("/users/{userId}", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertThat(result.getResponse().getContentAsString(),
                                containsString("User not found.")));
    }

    @Test
    void findAll_thenReturnOkAndListOfUSerDtos() throws Exception {
        when(userService.findAll()).thenReturn(List.of(userOutDto));

        String actualInString = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String expected = objectMapper.writeValueAsString(List.of(userOutDto));

        assertEquals(expected, actualInString);
    }

    @Test
    void update_whenUserExistInputValid_theReturnOkAndUserDto() throws Exception {
        userInputDto.setName("updated");
        userOutDto.setName("updated");
        when(userService.update(userInputDto, USER_ID)).thenReturn(userOutDto);

        String actualInString = mockMvc.perform(patch("/users/{userId}", USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userInputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UserDto actual = objectMapper.readValue(actualInString, UserDto.class);
        UserDto expected = userOutDto;

        assertThat(expected,samePropertyValuesAs(actual));
    }

    @Test
    void update_whenUserNotExistInputValid_theReturnNotFound() throws Exception {
        userInputDto.setName("updated");
        userOutDto.setName("updated");
        when(userService.update(userInputDto, USER_ID))
                .thenThrow(new ShareItElementNotFoundException("User not found."));

        mockMvc.perform(patch("/users/{userId}", USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userInputDto)))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof ShareItElementNotFoundException))
                .andExpect(result ->
                        assertThat(result.getResponse().getContentAsString(),
                                containsString("User not found.")));
    }

    @Test
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{userId}", USER_ID))
                .andExpect(status().isOk());
    }
}