package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.dto.RequestWithResponsesDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final String EXCEPTION_USER_NOT_FOUND_INFO = "User not found.";

    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestFullDto create(ItemRequestDto itemRequestDto, Long userId) {
        User user = getUserIfExists(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        return ItemRequestMapper.toItemRequestFullDto(itemRequest);
    }

    @Override
    public List<RequestWithResponsesDto> findAll(Long userId) {
        getUserIfExists(userId);
        Sort sort = Sort.by("created").descending();
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(userId, sort);
        List<RequestWithResponsesDto> requestWithResponsesDtoList = ItemRequestMapper.toRequestWithResponsesDtoList(requests);
        return completeWithResponses(requestWithResponsesDtoList);
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_USER_NOT_FOUND_INFO));
    }

    private List<RequestWithResponsesDto> completeWithResponses(List<RequestWithResponsesDto> requestDtoList) {
        for (RequestWithResponsesDto requestDto : requestDtoList) {
            Long id = requestDto.getId();
            List<Item> items = itemRepository.findAllByRequestId(id);
            requestDto.setResponses(ItemMapper.toResponsesList(items));
        }
        return requestDtoList;
    }
}
