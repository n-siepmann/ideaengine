/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Nick.Siepmann
 */
@Data
@AllArgsConstructor
public class Stats {

    private int daysUsed;
    private int currentStreak;
    private int maxStreak;
    private double averageDailyIdeas;
}
