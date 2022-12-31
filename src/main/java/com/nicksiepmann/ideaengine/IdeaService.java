/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.nicksiepmann.ideaengine.domain.ServiceUser;
import com.nicksiepmann.ideaengine.domain.ServiceUserRepository;
import com.nicksiepmann.ideaengine.domain.Card;
import com.nicksiepmann.ideaengine.domain.Idea;
import com.nicksiepmann.ideaengine.domain.Deck;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 *
 * @author Nick.Siepmann
 */
@Service
public class IdeaService {

    private Deck deck;
    private final Logger logger;
    private ServiceUserRepository serviceUserRepository;
    private ServiceUser user;
    private Emailer emailer;

    @Autowired
    public IdeaService(ServiceUserRepository serviceUserRepository) {
        this.logger = Logger.getLogger(IdeaService.class.getName());
        this.deck = null;
        this.emailer = new Emailer();
        try {
            this.deck = new Deck();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        this.serviceUserRepository = serviceUserRepository;
    }

    Card getCard() {
        return deck.getCard();
    }

    int getCardIndex() {
        return deck.getCardIndex();
    }

    ServiceUser getUser() {
        return this.user;
    }

    ServiceUser getUserFromDB(OAuth2User principal) {
        Optional<ServiceUser> found = this.serviceUserRepository.findByEmail(principal.getAttribute("email"));
        if (found.isPresent()) {
            this.user = found.get();
            checkDate(LocalDate.now());
        } else {
            ServiceUser newUser = new ServiceUser((principal.getAttribute("name")), principal.getAttribute("email"));
            this.serviceUserRepository.save(newUser);
            this.user = newUser;
        }
        return this.user;
    }

    void saveIdea(String idea) {
//        if (this.user.getDayIdeas().size() > 5) {
//            throw new IdeaException("Can only accept 5 ideas per day");
//        }
        this.user.getDayIdeas().add(new Idea(idea));
        this.serviceUserRepository.save(this.user);
    }

    ArrayList<Idea> getDayIdeas() {
        return this.user.getDayIdeas();
    }

    ArrayList<Idea> getPastIdeas() {
        return this.user.getPastIdeas();
    }

    ArrayList<Idea> getKeepers() {
        return this.user.getKeepers();
    }

    void checkDate(LocalDate now) {
//        LocalDate now = LocalDate.now();
        //if new day, move day ideas to past ideas and 
        if (user.getUpdated().isBefore(now)) {
            this.updateStats(this.user.getDayIdeas().size(), user.getUpdated().isBefore(now.minusDays(1)));
            this.resetDayIdeas();
            user.setTodayCards(this.newCards());
            user.setUpdated(now);
        }

        //if pastideas older than 14 days, delete
        if (!user.getPastIdeas().isEmpty()) {
            user.getPastIdeas().removeIf(s -> s.getCreated().isBefore(now.minusDays(14)));
        }

        //if keeper completed more than 7 days ago, delete
        if (!user.getKeepers().isEmpty()) {
            user.getKeepers().removeIf(s -> s.getCompleted() != null && s.getCompleted().isBefore(now.minusDays(7)));
        }
        this.serviceUserRepository.save(user);
    }

    void setIdeaCompleted(int index) {
        this.getKeepers().get(index).setCompleted(LocalDate.now());
        this.serviceUserRepository.save(user);
    }

    void setIdeaKeeper(int index) {
        this.getKeepers().add(this.getPastIdeas().get(index));
        this.getPastIdeas().remove(index);
        this.serviceUserRepository.save(user);
    }

    int[] newCards() {
        int[] newCards = new int[2];
        newCards[0] = this.getCardIndex();

        do {
            newCards[1] = this.getCardIndex();
        } while (newCards[0] == newCards[1]);
        return newCards;
    }

    private void resetDayIdeas() {
        user.getDayIdeas().stream().forEach(s -> user.getPastIdeas().add(s));
        user.setDayIdeas(new ArrayList<>());
    }

    private void updateStats(int todayIdeasCount, boolean streakBroken) {

        if (this.user.getAverageDailyIdeas() == 0) {
            this.user.setAverageDailyIdeas(todayIdeasCount);
        } else {
            this.user.setAverageDailyIdeas(((this.user.getAverageDailyIdeas() * (double) this.user.getDaysUsed()) + todayIdeasCount) / (this.user.getDaysUsed() + 1));
        }

        this.user.setDaysUsed(this.user.getDaysUsed() + 1);

        if (streakBroken) {
            this.user.setCurrentStreak(1);
        } else {
            this.user.setCurrentStreak(this.user.getCurrentStreak() + 1);
        }

        if (this.user.getCurrentStreak() > this.user.getMaxStreak()) {
            this.user.setMaxStreak(this.user.getCurrentStreak());
        }
    }

    void sendDailyEmail(String email, String name, double average, int maxStreak) { 
        String subject = "It's time to get creative";
        String textPart = "How many ideas can you come up with today? Your current average is " + String.format("%.1f", average) + " ideas per day, and your longest streak so far is " + maxStreak + ".";
        String htmlPart = "<h3>Visit <a href='https://ideaengine-373115.nw.r.appspot.com/today'>Idea Engine</a> to keep your streak going!</h3>";
        String customId = "IdeaEngineDaily";
        try {
            this.emailer.SendPrompt(email, name, subject, textPart, htmlPart, customId);
        } catch (MailjetException ex) {
            this.logger.log(Level.SEVERE, null, ex);
        } catch (MailjetSocketTimeoutException ex) {
            this.logger.log(Level.SEVERE, null, ex);
        }
    }

    void sendWeeklyEmail(String email, String name, int pastIdeasCount) {
        String subject = "Come check out your recent ideas";
        String textPart = "Everything starts from a good idea.";
        if (pastIdeasCount > 0 ){
            textPart = textPart + " You've come up with " + pastIdeasCount + "ideas in the last two weeks.";
        }
        String htmlPart = "<h3>Visit <a href='https://ideaengine-373115.nw.r.appspot.com/ideas'>Idea Engine</a> to review your recent ideas.</h3>";
        String customId = "IdeaEngineWeekly";
        try {
            this.emailer.SendPrompt(email, name, subject, textPart, htmlPart, customId);
        } catch (MailjetException ex) {
            this.logger.log(Level.SEVERE, null, ex);
        } catch (MailjetSocketTimeoutException ex) {
            this.logger.log(Level.SEVERE, null, ex);
        }
    }

}
