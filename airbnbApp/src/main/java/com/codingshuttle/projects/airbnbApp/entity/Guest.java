package com.codingshuttle.projects.airbnbApp.entity;

import com.codingshuttle.projects.airbnbApp.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class Guest {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
    @Column(nullable=false)
    private String name;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(nullable=false)
    private Integer age;

}
