package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.dto.RoomDto;

import java.util.List;

public interface RoomService {
    RoomDto createNewRoom(Long hotelId,RoomDto room);
    List<RoomDto> getAllRoomsInHotel(Long hotelId);
    RoomDto getRoomById(Long roomId);
    void deleteRoomById(Long roomId);


    RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto);
}
