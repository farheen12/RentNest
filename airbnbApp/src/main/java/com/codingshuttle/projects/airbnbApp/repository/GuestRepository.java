package com.codingshuttle.projects.airbnbApp.repository;

import com.codingshuttle.projects.airbnbApp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest,Long> {
}
