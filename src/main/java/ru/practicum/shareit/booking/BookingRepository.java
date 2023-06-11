package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

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

    String GET_CURRENT = " and b.start < ?2 and b.end > ?2 ";
    String GET_FUTURE = " and b.start > ?2 and b.end > ?2 ";
    String GET_PAST = " and b.start < ?2 and b.end < ?2 ";
    String GET_BY_STATUS = " and b.status = ?2 ";

    @Query(ALL_BY_BOOKER + ORDER_BY_DATE)
    List<Booking> findAllUserBookings(Long userId);

    @Query(ALL_BY_BOOKER + GET_CURRENT + ORDER_BY_DATE)
    List<Booking> findCurrentUserBookings(Long userId, LocalDateTime now);

    @Query(ALL_BY_BOOKER + GET_FUTURE + ORDER_BY_DATE)
    List<Booking> findFutureUserBookings(Long userId, LocalDateTime now);

    @Query(ALL_BY_BOOKER + GET_PAST + ORDER_BY_DATE)
    List<Booking> findPastUserBookings(Long userId, LocalDateTime now);

    @Query(ALL_BY_BOOKER + GET_BY_STATUS + ORDER_BY_DATE)
    List<Booking> findUserBookingsByStatus(Long userId, BookingStatus status);

    @Query(ALL_BY_OWNER + ORDER_BY_DATE)
    List<Booking> findAllUserItemsBookings(Long userId);

    @Query(ALL_BY_OWNER + GET_CURRENT + ORDER_BY_DATE)
    List<Booking> findCurrentUserItemsBookings(Long userId, LocalDateTime now);

    @Query(ALL_BY_OWNER + GET_FUTURE + ORDER_BY_DATE)
    List<Booking> findFutureUserItemsBookings(Long userId, LocalDateTime now);

    @Query(ALL_BY_OWNER + GET_PAST + ORDER_BY_DATE)
    List<Booking> findPastUserItemsBookings(Long userId, LocalDateTime now);

    @Query(ALL_BY_OWNER + GET_BY_STATUS + ORDER_BY_DATE)
    List<Booking> findUserItemsBookingsByStatus(Long userId, BookingStatus status);

    Booking findFirst1BookingByItemIdAndEndBefore(Long itemId, LocalDateTime now, Sort sort);

    Booking findFirst1BookingByItemIdAndStartAfter(Long itemId, LocalDateTime now, Sort sort);
}
