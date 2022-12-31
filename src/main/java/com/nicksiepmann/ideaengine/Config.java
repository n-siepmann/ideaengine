/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import com.nicksiepmann.ideaengine.domain.ServiceUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

/**
 *
 * @author Nick.Siepmann
 */
@Configuration
public class Config {

    @Bean
    @Autowired
    @SessionScope
    public IdeaService sessionIdeaService(ServiceUserRepository serviceUserRepository) {
        return new IdeaService(serviceUserRepository);
    }

}
