package com.nicksiepmann.ideaengine.domain;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */


import org.springframework.stereotype.Repository;
import com.google.cloud.spring.data.datastore.repository.DatastoreRepository;
import java.util.Optional;


/**
 *
 * @author Nick.Siepmann
 */

@Repository
public interface ServiceUserRepository extends DatastoreRepository<ServiceUser, Long> {    
    /**
     *
     * @param name
     * @return
     */
    Optional<ServiceUser> findByEmail(String email);

}
