package com.codingshuttle.projects.airbnbApp.repository;

import com.codingshuttle.projects.airbnbApp.dto.HotelPriceDto;
import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import com.codingshuttle.projects.airbnbApp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinRepository extends JpaRepository<HotelMinPrice,Long> {
    @Query("""
    SELECT DISTINCT new com.codingshuttle.projects.airbnbApp.dto.HotelPriceDto(
        i.hotel,
        AVG(i.price)
    )
    FROM Inventory i
    WHERE i.hotel.city = :city
    AND i.date BETWEEN :startDate AND :endDate
    AND i.hotel.active = true
    GROUP BY i.hotel.id
""")
    Page<HotelPriceDto> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
