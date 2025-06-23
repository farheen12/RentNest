package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.dto.RoomDto;
import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import com.codingshuttle.projects.airbnbApp.entity.Room;
import com.codingshuttle.projects.airbnbApp.entity.User;
import com.codingshuttle.projects.airbnbApp.exception.ResourceNotFoundException;
import com.codingshuttle.projects.airbnbApp.exception.UnAuthorizedException;
import com.codingshuttle.projects.airbnbApp.repository.HotelRepository;
import com.codingshuttle.projects.airbnbApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.spi.ResourceBundleControlProvider;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    @Override
    public RoomDto createNewRoom(Long hotelId,RoomDto room) {
        log.info("creating a new room in hotel:{}",hotelId);
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel Not Found with Id"+hotelId));
        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This User doesn't own this hotel");
        }
        Room roomEntity =modelMapper.map(room,Room.class);
        roomEntity.setHotel(hotel);
        roomRepository.save(roomEntity);
        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(roomEntity);
        }
        return modelMapper.map(roomEntity,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting list of rooms in hotel:{}",hotelId);
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel Not Found with Id"+hotelId));
        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This User doesn't own this hotel");
        }
        return hotel.getRooms()
                .stream()
                .map((element)->modelMapper.map(element,RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room by RoomId:{}",roomId);
        Room room=roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException("Room not found"+roomId));
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting a room by id {}",roomId);
        Room room=roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException("Room not found"+roomId));
        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorizedException("This User doesn't own this hotel");
        }
        boolean exists=roomRepository.existsById(roomId);
        if(!exists){
            throw new ResourceNotFoundException("Room not found"+roomId);
        }
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);

    }

    @Override
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+hotelId));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This user does not own this hotel");
        }
        Room room=roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException("Room not found"));
        modelMapper.map(roomDto,room);
        room.setId(roomId);
        room=roomRepository.save(room);
        return modelMapper.map(room,RoomDto.class);
    }
}
;