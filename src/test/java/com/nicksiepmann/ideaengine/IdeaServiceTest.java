/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author Nick.Siepmann
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
public class IdeaServiceTest {

    IdeaService underTest;

    @Autowired
    ServiceUserRepository serviceUserRepository;

    @Mock
    OAuth2User principal;

    @BeforeAll
    public void setUpClass() {
        this.underTest = new IdeaService(serviceUserRepository);
    }

    @AfterAll
    public void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        if (this.serviceUserRepository.findByEmail("email@gmail.com").isPresent()) {
            this.serviceUserRepository.delete(this.serviceUserRepository.findByEmail("email@gmail.com").get());
        }
    }

    @Test
    void canGetCard() {
        Card card = this.underTest.getCard();
        assertThat(card.getName().length() > 0);
    }

    @Test
    void canGetServiceUser() {
        given(principal.getAttribute("name")).willReturn("User Name");
        given(principal.getAttribute("email")).willReturn("email@gmail.com");
        ServiceUser user = this.underTest.getUserFromDB(principal);
        assertEquals("User Name", user.getName());
        user.setReceiveDailyPrompt(true);
    }

    @Test
    void canSaveAndGetIdea() {
        given(principal.getAttribute("name")).willReturn("User Name");
        given(principal.getAttribute("email")).willReturn("email@gmail.com");
        ServiceUser user = this.underTest.getUserFromDB(principal);

        String ideatext = "current idea";
        this.underTest.saveIdea(ideatext);
        String ideatext2 = "current idea2";
        this.underTest.saveIdea(ideatext2);
        assertEquals(ideatext, this.underTest.getDayIdeas().get(0).getText());
        assertEquals(LocalDate.now().toString(), this.underTest.getDayIdeas().get(0).getCreated().toString());

        this.underTest.checkDate(LocalDate.now().plusDays(1));
        assertEquals(0, this.underTest.getDayIdeas().size());
        assertEquals(2, this.underTest.getPastIdeas().size());
        this.underTest.setIdeaKeeper(0);
        assertEquals(1, this.underTest.getKeepers().size());
        this.underTest.checkDate(LocalDate.now().plusDays(17));
        assertEquals(0, this.underTest.getPastIdeas().size());

        this.underTest.setIdeaCompleted(0);
        this.underTest.checkDate(LocalDate.now().plusDays(18));
        assertEquals(0, this.underTest.getKeepers().size());
    }

    @Test
    void canKeepStats() {
        given(principal.getAttribute("name")).willReturn("User Name");
        given(principal.getAttribute("email")).willReturn("email@gmail.com");
        ServiceUser user = this.underTest.getUserFromDB(principal);

        String ideatext = "current idea";
        this.underTest.saveIdea(ideatext);
        this.underTest.checkDate(LocalDate.now().plusDays(1));
        assertEquals(1, this.underTest.getUser().getCurrentStreak());
        assertEquals(1, this.underTest.getUser().getMaxStreak());
        assertEquals(1, this.underTest.getUser().getAverageDailyIdeas());
        assertEquals(1, this.underTest.getUser().getDaysUsed());

        this.underTest.saveIdea(ideatext + "2");
        this.underTest.saveIdea(ideatext + "3");
        this.underTest.checkDate(LocalDate.now().plusDays(2));
        assertEquals(2, this.underTest.getUser().getCurrentStreak());
        assertEquals(2, this.underTest.getUser().getMaxStreak());
        assertEquals(2, this.underTest.getUser().getDaysUsed());
        assertEquals(1.5, this.underTest.getUser().getAverageDailyIdeas());

        this.underTest.saveIdea(ideatext + "4");
        this.underTest.saveIdea(ideatext + "5");
        this.underTest.saveIdea(ideatext + "6");
        this.underTest.checkDate(LocalDate.now().plusDays(4));
        assertEquals(1, this.underTest.getUser().getCurrentStreak());
        assertEquals(2, this.underTest.getUser().getMaxStreak());
        assertEquals(3, this.underTest.getUser().getDaysUsed());
        assertEquals(2, this.underTest.getUser().getAverageDailyIdeas());

        this.underTest.saveIdea(ideatext + "7");
        this.underTest.checkDate(LocalDate.now().plusDays(5));
        this.underTest.saveIdea(ideatext + "8");
        this.underTest.checkDate(LocalDate.now().plusDays(6));
        this.underTest.saveIdea(ideatext + "9");
        this.underTest.checkDate(LocalDate.now().plusDays(7));
        assertEquals(4, this.underTest.getUser().getCurrentStreak());
        assertEquals(4, this.underTest.getUser().getMaxStreak());
        this.underTest.saveIdea(ideatext + "10");
        this.underTest.checkDate(LocalDate.now().plusDays(9));
        assertEquals(1, this.underTest.getUser().getCurrentStreak());
        assertEquals(4, this.underTest.getUser().getMaxStreak());
    }
}
