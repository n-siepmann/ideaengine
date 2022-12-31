/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine.domain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nick.Siepmann
 */
public class Deck {

    private ArrayList<Card> cards;
    private Random random;
    private final Logger logger;

    public Deck() throws IOException {
        this.random = new Random();
        this.logger = Logger.getLogger(Deck.class.getName());
        Type arrayListOfCards = new TypeToken<ArrayList<Card>>() {
        }.getType();
        try {
            this.cards = new Gson().fromJson(Files.newBufferedReader(Paths.get(this.getClass().getClassLoader().getResource("static/tarot.json").toURI())), arrayListOfCards);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Deck.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Card getCard() {
        return this.cards.get(this.random.nextInt(this.cards.size()));
    }

    public int getCardIndex() {
        return this.random.nextInt(this.cards.size());
    }
    
    public Card getCardByIndex(int index){
        return this.cards.get(index);
    }

}
