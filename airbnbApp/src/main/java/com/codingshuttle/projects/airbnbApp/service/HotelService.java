package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.dto.HotelDto;
import com.codingshuttle.projects.airbnbApp.dto.HotelInfoDto;
import com.codingshuttle.projects.airbnbApp.entity.Hotel;

import java.util.List;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);
    HotelDto getHotelById(Long id);
    HotelDto updateHotelById(Long id,HotelDto hotelDto);
    Boolean deleteHotelById(Long id);
    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDto> getAllHotels();
}
