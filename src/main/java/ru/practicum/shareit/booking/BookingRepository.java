package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    String ALL_BY_BOOKER =
            " select b from bookings b " +
                    " inner join b.booker " +
                    " where b.booker.id = ?1 ";

    String ALL_BY_OWNER =
            " select b from bookings b " +
                    " inner join b.item " +
                    " inner join b.item.owner " +
                    " where b.item.owner.id = ?1 ";

    String ORDER_BY_DATE = " order by b.start desc";

    String GET_CURRENT = " and b.start < CURRENT_TIMESTAMP and b.end > CURRENT_TIMESTAMP ";
    String GET_FUTURE = " and b.start > CURRENT_TIMESTAMP and b.end > CURRENT_TIMESTAMP ";
    String GET_PAST = " and b.start < CURRENT_TIMESTAMP and b.end < CURRENT_TIMESTAMP ";
    String GET_WAITING = " and b.status = ru.practicum.shareit.booking.model.BookingStatus.WAITING ";
    String GET_REJECTED = " and b.status = ru.practicum.shareit.booking.model.BookingStatus.REJECTED ";

    @Query(ALL_BY_BOOKER + ORDER_BY_DATE)
    List<Booking> findAllUserBookings(Long userId);

    @Query(ALL_BY_BOOKER + GET_CURRENT + ORDER_BY_DATE)
    List<Booking> findCurrentUserBookings(Long userId);

    @Query(ALL_BY_BOOKER + GET_FUTURE + ORDER_BY_DATE)
    List<Booking> findFutureUserBookings(Long userId);

    @Query(ALL_BY_BOOKER + GET_PAST + ORDER_BY_DATE)
    List<Booking> findPastUserBookings(Long userId);

    @Query(ALL_BY_BOOKER + GET_WAITING + ORDER_BY_DATE)
    List<Booking> findUserBookingsWaiting(Long userId);

    @Query(ALL_BY_BOOKER + GET_REJECTED + ORDER_BY_DATE)
    List<Booking> findUserBookingsRejected(Long userId);

    @Query(ALL_BY_OWNER + ORDER_BY_DATE)
    List<Booking> findAllUserItemsBookings(Long userId);

    @Query(ALL_BY_OWNER + GET_CURRENT + ORDER_BY_DATE)
    List<Booking> findCurrentUserItemsBookings(Long userId);

    @Query(ALL_BY_OWNER + GET_FUTURE + ORDER_BY_DATE)
    List<Booking> findFutureUserItemsBookings(Long userId);

    @Query(ALL_BY_OWNER + GET_PAST + ORDER_BY_DATE)
    List<Booking> findPastUserItemsBookings(Long userId);

    @Query(ALL_BY_OWNER + GET_WAITING + ORDER_BY_DATE)
    List<Booking> findUserItemsBookingsWaiting(Long userId);

    @Query(ALL_BY_OWNER + GET_REJECTED + ORDER_BY_DATE)
    List<Booking> findUserItemsBookingsRejected(Long userId);

    Optional<Booking> findFirst1BookingByItemIdAndStatusAndStartBefore(Long itemId, BookingStatus status, LocalDateTime now, Sort sort);

    Optional<Booking> findFirst1BookingByItemIdAndStatusAndStartAfter(Long itemId, BookingStatus status, LocalDateTime now, Sort sort);

    Optional<Booking> findFirst1BookingByBookerIdAndItemIdAndStatusAndStartBefore(Long bookerId, Long itemId, BookingStatus status, LocalDateTime now);

}

