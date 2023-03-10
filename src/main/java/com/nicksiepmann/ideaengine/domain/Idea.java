/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Nick.Siepmann
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Idea {

    private String text;
    private boolean keep;
    private LocalDate created;
    private LocalDate completed;

    public Idea(String text) {
        this.text = text;
        this.created = LocalDate.now();
    }
    

}
