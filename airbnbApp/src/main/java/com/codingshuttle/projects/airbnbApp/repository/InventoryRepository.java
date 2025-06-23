package com.codingshuttle.projects.airbnbApp.repository;

import com.codingshuttle.projects.airbnbApp.dto.HotelPriceDto;
import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import com.codingshuttle.projects.airbnbApp.entity.HotelMinPrice;
import com.codingshuttle.projects.airbnbApp.entity.Inventory;
import com.codingshuttle.projects.airbnbApp.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
void deleteByRoom(Room room);

//@Query("""
//        SELECT DISTINCT com.codingshuttle.projects.airbnbApp.dto.HotelPriceDto(i.hotel,AVG(i.price))\s
//            FROM HotelMinPrice i\s
//            WHERE i.city = :city\s
//                AND i.date BETWEEN :startDate AND :endDate
//                AND i.hotel.active = true\s
//                GROUP BY i.hotel
//        """)
//Page<HotelPriceDto> findHotelsWithAvailableInventory(
//        @Param("city") String city,
//        @Param("startDate") LocalDate startDate,
//        @Param("endDate") LocalDate endDate,
//        @Param("roomsCount") Integer roomsCount,
//        @Param ("dateCount") Integer dateCount,
//        Pageable pageable
//        );
@Query("""
        Select i from Inventory i where i.room.id= :roomId AND i.date between :startDate and :endDate AND i.closed = false
                AND (i.totalCount -i.bookedCount- i.reservedCount) >= :roomsCount
        """)
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<Inventory> findAndLockAvailableInventory(
        @Param("roomId") Long roomId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("roomsCount") Integer roomsCount
);
        @Query("""
        Select i from Inventory i where i.room.id= :roomId AND i.date between :startDate and :endDate AND i.closed = false
                AND (i.totalCount -i.bookedCount) >= :roomsCount
        """)
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        List<Inventory> findAndLockReservedInventory(
                @Param("roomId") Long roomId,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("roomsCount") Integer roomsCount
        );
        @Modifying
        @Query("""
        Update Inventory i
        Set i.reservedCount=i.reservedCount + :numberOfRooms
        where i.room.id= :roomId
        and i.date between :startDate and :endDate
        and (i.totalCount-i.bookedCount-i.reservedCount) >= :numberOfRooms
        and i.closed=false
        """)
        void initBooking(@Param("roomId") Long roomId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate")LocalDate endDate,
                            @Param("numberOfRooms") int numberOfRooms);
@Modifying
@Query("""
        Update Inventory i
        Set i.reservedCount=i.reservedCount - :numberOfRooms,
        i.bookedCount=i.bookedCount + :numberOfRooms
        where i.room.id= :roomId
        and i.date between :startDate and :endDate
        and (i.totalCount-i.bookedCount) >= :numberOfRooms
        and i.reservedCount >= :numberOfRooms
        and i.closed=false
        """)
void confirmBooking(@Param("roomId") Long roomId,
                    @Param("startDate") LocalDate startDate,
                    @Param("endDate")LocalDate endDate,
                    @Param("numberOfRooms") int numberOfRooms);

        @Modifying
        @Query("""
        Update Inventory i
        Set i.bookedCount=i.bookedCount - :numberOfRooms
        where i.room.id= :roomId
        and i.date between :startDate and :endDate
        and (i.totalCount-i.bookedCount) >= :numberOfRooms
        and i.closed=false
        """)
        void cancelBooking(@Param("roomId") Long roomId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate")LocalDate endDate,
                            @Param("numberOfRooms") int numberOfRooms);

List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);
List<Inventory> findByRoomOrderByDate(Room room);
@Query("""
        Select i from Inventory i
        Where i.room.id=:roomId
        and i.date between :startDate and :endDate
        """)
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        List<Inventory> getInventoryAndLockBeforeUpdate(@Param("roomId") Long roomId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate")LocalDate endDate);
@Modifying
@Query("""
        Update Inventory i
        Set i.surgeFactor=:surgeFactor,
        i.closed=:closed
        Where i.room.id=:roomId
        and i.date between :startDate and :endDate
        """)
        void updateInventory(@Param("roomId") Long roomId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate")LocalDate endDate,
                           @Param("closed") boolean closed,
                           @Param("surgeFactor")
                           BigDecimal surgeFactor);



}

