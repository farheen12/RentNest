package com.codingshuttle.projects.airbnbApp.dto;

import com.codingshuttle.projects.airbnbApp.entity.HotelContactInfo;
import com.codingshuttle.projects.airbnbApp.entity.Room;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class HotelDto {

    private Long id;
    private String name;
    private String city;
    private String[] photos;
    private String[] amenities;
    private HotelContactInfo contactInfo;
    private Boolean active;


}
