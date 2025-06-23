package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.entity.Booking;
import org.springframework.stereotype.Service;


public interface CheckOutService {

String getCheckOutSession(Booking booking, String successUrl, String failureUrl);

}
