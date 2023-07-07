package ru.practicum.shareit.request.service;

import ru.practicum.shareit.exception.ShareItElementNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.dto.RequestWithItemsDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final String EXCEPTION_REQUEST_NOT_FOUND_INFO = "Request not found";
    private static final String EXCEPTION_USER_NOT_FOUND_INFO = "User not found.";
    private static final Sort SORT = Sort.by("created").descending();

    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public RequestWithItemsDto create(ItemRequestInputDto itemRequestInputDto, Long userId) {
        User user = getUserIfExists(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestInputDto, user);
        return ItemRequestMapper.toRequestWithItemsDto(requestRepository.save(request));
    }

    @Override
    public List<RequestWithItemsDto> findAll(Long userId) {
        getUserIfExists(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequesterId(userId, SORT);
        List<RequestWithItemsDto> requestsWithItems = ItemRequestMapper.toRequestWithItemsDtoList(requests);
        return complete(requestsWithItems);
    }

    @Override
    public List<RequestWithItemsDto> findAll(Long userId, int from, int size) {
        getUserIfExists(userId);
        Page<ItemRequest> requestPages = requestRepository.findAll(userId, pageRequestOf(from, size, SORT));
        List<RequestWithItemsDto> requestsWithItems = ItemRequestMapper.toRequestWithItemsDtoList(requestPages);
        return complete(requestsWithItems);
    }

    @Override
    public RequestWithItemsDto getById(Long userId, Long requestId) {
        getUserIfExists(userId);
        ItemRequest request = getItemRequestIfExists(requestId);
        RequestWithItemsDto requestWithItems = ItemRequestMapper.toRequestWithItemsDto(request);
        return complete(requestWithItems);
    }

    private ItemRequest getItemRequestIfExists(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_REQUEST_NOT_FOUND_INFO));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ShareItElementNotFoundException(EXCEPTION_USER_NOT_FOUND_INFO));
    }

    private RequestWithItemsDto complete(RequestWithItemsDto request) {
        Long requestId = request.getId();
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        request.setItems(ItemMapper.toItemResponseInRequestDtoList(items));
        return request;
    }

    private List<RequestWithItemsDto> complete(List<RequestWithItemsDto> requests) {
        return requests.stream().map(this::complete).collect(Collectors.toList());
    }

    private static Pageable pageRequestOf(int from, int size, Sort sort) {
        int page = from / size;
        return PageRequest.of(page, size, sort);
    }
}
