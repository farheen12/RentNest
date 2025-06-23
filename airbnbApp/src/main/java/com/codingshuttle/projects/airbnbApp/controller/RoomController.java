package com.codingshuttle.projects.airbnbApp.controller;

import com.codingshuttle.projects.airbnbApp.dto.RoomDto;
import com.codingshuttle.projects.airbnbApp.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/hotels/{hotelId}/rooms")
public class RoomController {
    private final RoomService roomService;
    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom(@PathVariable Long hotelId, @RequestBody RoomDto room){
        return new ResponseEntity<>(roomService.createNewRoom(hotelId,room), HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId){
        return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
    }
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long roomId){
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoomById(@PathVariable Long roomId){
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDto> updateRoomById(@PathVariable Long hotelId,@PathVariable Long roomId,@RequestBody RoomDto roomDto){

        return ResponseEntity.ok(roomService.updateRoomById(hotelId,roomId,roomDto));

    }
}
