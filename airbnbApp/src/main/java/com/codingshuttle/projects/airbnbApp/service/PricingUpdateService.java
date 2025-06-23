package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import com.codingshuttle.projects.airbnbApp.entity.HotelMinPrice;
import com.codingshuttle.projects.airbnbApp.entity.Inventory;
import com.codingshuttle.projects.airbnbApp.repository.HotelMinRepository;
import com.codingshuttle.projects.airbnbApp.repository.HotelRepository;
import com.codingshuttle.projects.airbnbApp.repository.InventoryRepository;
import com.codingshuttle.projects.airbnbApp.repository.RoomRepository;
import com.codingshuttle.projects.airbnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingService pricingService;
    private final HotelMinRepository hotelMinRepository;
    @Scheduled(cron="0 0 * * * *")
    public void updatePrices(){
           int page=0;
           int batchSize=100;
           while(true){
               Page<Hotel> hotelPage=hotelRepository.findAll(PageRequest.of(page,batchSize));
               if(hotelPage.isEmpty()){
                   break;
               }
               hotelPage.getContent().forEach(hotel->updateHotelPrices(hotel));
           }
    }
    private void updateHotelPrices(Hotel hotel){
        LocalDate startDate= LocalDate.now();
        LocalDate endDate=LocalDate.now().plusYears(1);
        List<Inventory> inventoryList=inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);
        updateInventoryPrices(inventoryList);
        updateHotelMinPrices(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrices(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        // Step 1: Calculate minimum price for each date
        Map<LocalDate, BigDecimal> dailyMinPrices = new HashMap<>();

        for (Inventory inventory : inventoryList) {
            LocalDate date = inventory.getDate();
            BigDecimal price = inventory.getPrice();

            // If we already have a price for this date, keep the lower one
            if (dailyMinPrices.containsKey(date)) {
                BigDecimal currentMin = dailyMinPrices.get(date);
                if (price.compareTo(currentMin) < 0) {
                    dailyMinPrices.put(date, price);
                }
            } else {
                dailyMinPrices.put(date, price);
            }
        }

        // Step 2: Update or create HotelMinPrice records
        List<HotelMinPrice> hotelPrices = new ArrayList<>();

        for (Map.Entry<LocalDate, BigDecimal> entry : dailyMinPrices.entrySet()) {
            LocalDate date = entry.getKey();
            BigDecimal price = entry.getValue();

            // Try to find existing record
            Optional<HotelMinPrice> existingRecord = hotelMinRepository.findByHotelAndDate(hotel, date);
            HotelMinPrice hotelPrice;

            if (existingRecord.isPresent()) {
                hotelPrice = existingRecord.get();
            } else {
                hotelPrice = new HotelMinPrice(hotel, date);
            }

            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        }

        // Step 3: Save all records
        hotelMinRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList) {
    inventoryList.forEach(inventory->{
        BigDecimal dynamicPrice=pricingService.calculateDynamicPricing(inventory);
        inventory.setPrice(dynamicPrice);

    });
    inventoryRepository.saveAll(inventoryList);
    }

}
