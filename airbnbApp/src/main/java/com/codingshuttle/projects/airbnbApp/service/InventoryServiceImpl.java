package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.dto.*;
import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import com.codingshuttle.projects.airbnbApp.entity.Inventory;
import com.codingshuttle.projects.airbnbApp.entity.Room;
import com.codingshuttle.projects.airbnbApp.entity.User;
import com.codingshuttle.projects.airbnbApp.exception.ResourceNotFoundException;
import com.codingshuttle.projects.airbnbApp.repository.HotelMinRepository;
import com.codingshuttle.projects.airbnbApp.repository.InventoryRepository;
import com.codingshuttle.projects.airbnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.codingshuttle.projects.airbnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final RoomRepository roomRepository;

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinRepository hotelMinRepository;
    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today= LocalDate.now();
        LocalDate endDate=today.plusYears(1);
        for(;!today.isAfter(endDate);today=today.plusDays(1)){
            Inventory inventory=Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventory of room{}",room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city, from {} to {}",hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate());
        Pageable pageable= PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        long dateCount= ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate())+1;
        Page<HotelPriceDto> hotelPage=hotelMinRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate(),pageable);
        return hotelPage;
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        Room room=roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException("Room not found"));
        User user=getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("Not owner of this room");
        return inventoryRepository.findByRoomOrderByDate(room).stream().map((element)->modelMapper.map(element, InventoryDto.class)).toList();

    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        Room room=roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException("Room not found"));
        User user=getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("Not owner of this room");
        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());
        inventoryRepository.updateInventory(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate(),updateInventoryRequestDto.getClosed(),updateInventoryRequestDto.getSurgeFactor());
    }

}
