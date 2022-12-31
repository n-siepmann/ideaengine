/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Nick.Siepmann
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class Card {

    private String name;
    private String gloss;
    private String interpretation;
}
