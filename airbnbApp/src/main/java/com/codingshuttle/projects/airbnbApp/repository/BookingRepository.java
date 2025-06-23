package com.codingshuttle.projects.airbnbApp.repository;

import com.codingshuttle.projects.airbnbApp.dto.BookingDto;
import com.codingshuttle.projects.airbnbApp.entity.Booking;
import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import com.codingshuttle.projects.airbnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime );

    List<BookingDto> findByUser(User user);
}
