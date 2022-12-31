/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Nick.Siepmann
 */
@Controller
public class IdeaController {
    private IdeaService service;

    @Autowired
    public IdeaController(IdeaService sessionIdeaService) {
        this.service = sessionIdeaService;
    }   
    
}
