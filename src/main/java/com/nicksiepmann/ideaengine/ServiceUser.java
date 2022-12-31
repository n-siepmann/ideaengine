package com.nicksiepmann.ideaengine;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import lombok.Data;
import org.springframework.data.annotation.Id;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Nick.Siepmann
 */
@Entity(name = "users")
@Data
class ServiceUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    private String name;
    private final String email;
    private LocalDate updated;
    private ArrayList<Idea> pastIdeas;
    private ArrayList<Idea> dayIdeas;
    private ArrayList<Idea> keepers;
    private boolean receiveDailyPrompt;
    private boolean receiveWeeklyPrompt;
    private int[] todayCards;

    private int daysUsed;
    private int currentStreak;
    private int maxStreak;
    private double averageDailyIdeas;
    
    public ServiceUser(String name, String email) {
        this.name = name;
        this.email = email;
        this.pastIdeas = new ArrayList<>();
        this.dayIdeas = new ArrayList<>();
        this.keepers = new ArrayList<>();
        this.todayCards = new int[2];
        this.updated = LocalDate.now();
    }

}
