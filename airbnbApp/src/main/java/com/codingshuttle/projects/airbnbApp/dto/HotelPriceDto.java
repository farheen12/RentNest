package com.codingshuttle.projects.airbnbApp.dto;

import com.codingshuttle.projects.airbnbApp.entity.Hotel;
import lombok.*;

import java.math.BigDecimal;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HotelPriceDto {

    private Hotel hotel;
    private Double price;
}
