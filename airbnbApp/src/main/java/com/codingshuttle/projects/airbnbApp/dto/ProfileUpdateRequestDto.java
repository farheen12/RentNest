package com.codingshuttle.projects.airbnbApp.dto;

import com.codingshuttle.projects.airbnbApp.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;

}
