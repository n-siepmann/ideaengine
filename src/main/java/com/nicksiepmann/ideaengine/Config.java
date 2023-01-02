/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.cloud.spring.data.datastore.core.convert.DatastoreCustomConversions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nicksiepmann.ideaengine.domain.*;
import com.nicksiepmann.ideaengine.domain.ServiceUserRepository;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.annotation.SessionScope;

/**
 *
 * @author Nick.Siepmann
 */
@Configuration
public class Config {
    
    private static final Gson gson = Converters.registerLocalDate(new GsonBuilder()).create();
    
    @Bean
    @Autowired
    @SessionScope
    public IdeaService sessionIdeaService(ServiceUserRepository serviceUserRepository) {
        return new IdeaService(serviceUserRepository);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/oauth2/**", "/error", "/welcome", "/goodbye", "/static/**", "/*.css", "/logo_white.svg", "/favicon*.png", "/android*.png").permitAll()
                .anyRequest().authenticated()
                .and().oauth2Login().loginPage("/welcome").defaultSuccessUrl("/", true);
        return http.build();
        
    }
    
    @Bean
    public DatastoreCustomConversions datastoreCustomConversions() {
        return new DatastoreCustomConversions(
                Arrays.asList(
                        IDEA_STRING_CONVERTER,
                        STRING_IDEA_CONVERTER,
                        //                        CARD_STRING_CONVERTER,
                        //                        STRING_CARD_CONVERTER,
                        STATS_STRING_CONVERTER,
                        STRING_STATS_CONVERTER));
    }
    static final Converter<Idea, String> IDEA_STRING_CONVERTER
            = new Converter<Idea, String>() {
        @Override
        public String convert(Idea idea) {
            return gson.toJson(idea);
        }
    };
    
    static final Converter<String, Idea> STRING_IDEA_CONVERTER
            = new Converter<String, Idea>() {
        @Override
        public Idea convert(String json) {
            return gson.fromJson(json, Idea.class);
        }
    };
    
    static final Converter<Stats, String> STATS_STRING_CONVERTER
            = new Converter<Stats, String>() {
        @Override
        public String convert(Stats stats) {
            return gson.toJson(stats);
        }
    };
    
    static final Converter<String, Stats> STRING_STATS_CONVERTER
            = new Converter<String, Stats>() {
        @Override
        public Stats convert(String json) {
            return gson.fromJson(json, Stats.class);
        }
    };
    
}
