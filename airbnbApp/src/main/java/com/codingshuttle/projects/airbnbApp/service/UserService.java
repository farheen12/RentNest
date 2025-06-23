package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.dto.ProfileUpdateRequestDto;
import com.codingshuttle.projects.airbnbApp.dto.UserDto;
import com.codingshuttle.projects.airbnbApp.entity.User;

public interface UserService {
    User getUserById(Long userId);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
