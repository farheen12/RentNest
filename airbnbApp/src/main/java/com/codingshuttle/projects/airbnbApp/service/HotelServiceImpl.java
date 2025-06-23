package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.dto.HotelDto;
import com.codingshuttle.projects.airbnbApp.dto.HotelInfoDto;
import com.codingshuttle.projects.airbnbApp.dto.RoomDto;
import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import com.codingshuttle.projects.airbnbApp.entity.Room;
import com.codingshuttle.projects.airbnbApp.entity.User;
import com.codingshuttle.projects.airbnbApp.exception.ResourceNotFoundException;
import com.codingshuttle.projects.airbnbApp.exception.UnAuthorizedException;
import com.codingshuttle.projects.airbnbApp.repository.HotelRepository;
import com.codingshuttle.projects.airbnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.codingshuttle.projects.airbnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("creating a new hotel:{}",hotelDto.getName());
        Hotel hotel=modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);
        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        hotel=hotelRepository.save(hotel);
        log.info("created a new hotel:{}",hotelDto.getName());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting a hotel by ID:{}",id);
        Hotel hotel=hotelRepository
        .findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID:"+id));
        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This User doesn't own this hotel");
        }
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("updating hotel by Id:{}",id);

        Hotel hotel=hotelRepository
                .findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID:"+id));
        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This User doesn't own this hotel");
        }
        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel=hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Transactional
    public Boolean deleteHotelById(Long id){
        log.info("Deleting hotel by Id:{}",id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("hotel not found"));
        boolean exists=hotelRepository.existsById(id);
        if(!exists){
            throw new ResourceNotFoundException("Hotel not found with ID:"+id);
        }
        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This User doesn't own this hotel");
        }
        for(Room room:hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
        return true;
    }

    @Override
    public void activateHotel(Long hotelId) {
        log.info("Activating hotel with Id:{}",hotelId);
        Hotel hotel=hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Not Found"));
        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorizedException("This User doesn't own this hotel");
        }
        hotel.setActive(true);
        for(Room room:hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel=hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Not Found"));
        List<RoomDto> rooms=hotel.getRooms()
                .stream().map((element)->modelMapper.map(element,RoomDto.class)).toList();
        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user=getCurrentUser();
        List<Hotel> hotels=hotelRepository.findByOwner(user);
        return hotels.stream().map(hotel->modelMapper.map(hotel,HotelDto.class)).toList();
    }

}
