package com.codingshuttle.projects.airbnbApp.controller;


import com.codingshuttle.projects.airbnbApp.dto.HotelDto;
import com.codingshuttle.projects.airbnbApp.dto.HotelInfoDto;
import com.codingshuttle.projects.airbnbApp.dto.HotelPriceDto;
import com.codingshuttle.projects.airbnbApp.dto.HotelSearchRequest;
import com.codingshuttle.projects.airbnbApp.service.HotelService;
import com.codingshuttle.projects.airbnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){
        Page<HotelPriceDto> page=inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }
    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}
