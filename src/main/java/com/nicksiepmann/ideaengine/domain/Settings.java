/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Nick.Siepmann
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Settings {

    private boolean receiveDailyPrompt;
    private boolean receiveWeeklyPrompt;
}
