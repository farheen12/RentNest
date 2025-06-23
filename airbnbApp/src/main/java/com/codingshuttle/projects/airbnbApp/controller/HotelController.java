package com.codingshuttle.projects.airbnbApp.controller;

import com.codingshuttle.projects.airbnbApp.dto.BookingDto;
import com.codingshuttle.projects.airbnbApp.dto.HotelDto;
import com.codingshuttle.projects.airbnbApp.dto.HotelReportDto;
import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import com.codingshuttle.projects.airbnbApp.entity.User;
import com.codingshuttle.projects.airbnbApp.repository.HotelRepository;
import com.codingshuttle.projects.airbnbApp.service.BookingService;
import com.codingshuttle.projects.airbnbApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.codingshuttle.projects.airbnbApp.util.AppUtils.getCurrentUser;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
// this is for autowiring
public class HotelController {
    private final HotelRepository hotelRepository;
    private final HotelService hotelService;
    private final BookingService bookingService;
    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto){
        log.info("Attempting to create a new hotel with name:{}",hotelDto.getName());
        HotelDto hotel=hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }
    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId){
        HotelDto hotel=hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotel);
    }
    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId,@RequestBody HotelDto hotelDto){
        HotelDto hotel=hotelService.updateHotelById(hotelId,hotelDto);
        return ResponseEntity.ok(hotel);
    }
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId){
        Boolean isDeleted=hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{hotelId}")
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId){
        hotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels(){
        return ResponseEntity.ok(hotelService.getAllHotels());
    }
    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingsByHotelId(Long hotelId){
    return ResponseEntity.ok(bookingService.getAllBookingsByHotel(hotelId));}
    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getHotelReportDto(Long hotelId, @RequestParam(required=false) LocalDate startDate, @RequestParam(required=false)LocalDate endDate){
       if(startDate==null) startDate=LocalDate.now().minusMonths(1);
       if(endDate==null) endDate=LocalDate.now();
        return ResponseEntity.ok(bookingService.getHotelReport(hotelId,startDate,endDate));
    }
}
