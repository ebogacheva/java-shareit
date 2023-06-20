package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final String EXCEPTION_REQUEST_NOT_FOUND_INFO = "Request not found";
    private static final String EXCEPTION_USER_NOT_FOUND_INFO = "User not found.";
    private static final Sort SORT = Sort.by("created").descending();


    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestFullDto create(ItemRequestDto itemRequestDto, Long userId) {
        User user = getUserIfExists(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        return ItemRequestMapper.toItemRequestFullDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<RequestWithResponsesDto> findAll(Long userId) {
        getUserIfExists(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(userId, SORT);
        List<RequestWithResponsesDto> requestsWithResponses = ItemRequestMapper.toRequestWithResponsesDtoList(requests);
        return completeDtoList(requestsWithResponses);
    }

    @Override
    public List<RequestWithResponsesDto> findAll(Long userId, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, SORT);
        Page<ItemRequest> requestPages = itemRequestRepository.findAll(userId, pageable);
        List<RequestWithResponsesDto> requestWithResponses = ItemRequestMapper.toRequestWithResponsesDtoList(requestPages);
        return completeDtoList(requestWithResponses);

    }

    @Override
    public RequestWithResponsesDto getById(Long requestId) {
        ItemRequest request = getItemRequestIfExists(requestId);
        return ItemRequestMapper.toRequestWithResponsesDto(request);
    }

    private ItemRequest getItemRequestIfExists(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_REQUEST_NOT_FOUND_INFO));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_USER_NOT_FOUND_INFO));
    }

    private RequestWithResponsesDto completeWithResponses(RequestWithResponsesDto requestDto) {
        Long id = requestDto.getId();
        List<Item> items = itemRepository.findAllByRequestId(id);
        requestDto.setResponses(ItemMapper.toResponsesList(items));
        return requestDto;
    }

    private List<RequestWithResponsesDto> completeDtoList(List<RequestWithResponsesDto> requestDtos) {
        return requestDtos.stream().map(this::completeWithResponses).collect(Collectors.toList());
    }
}
