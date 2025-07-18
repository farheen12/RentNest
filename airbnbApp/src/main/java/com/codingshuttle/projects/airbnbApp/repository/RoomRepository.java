package com.codingshuttle.projects.airbnbApp.repository;

import com.codingshuttle.projects.airbnbApp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {

}
