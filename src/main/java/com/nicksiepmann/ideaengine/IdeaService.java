/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import com.mailjet.client.MailjetResponse;
import com.nicksiepmann.ideaengine.domain.Stats;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.nicksiepmann.ideaengine.domain.ServiceUser;
import com.nicksiepmann.ideaengine.domain.ServiceUserRepository;
import com.nicksiepmann.ideaengine.domain.Card;
import com.nicksiepmann.ideaengine.domain.Idea;
import com.nicksiepmann.ideaengine.domain.Deck;
import com.nicksiepmann.ideaengine.domain.Settings;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private ServiceUser user;
    private Emailer emailer;
    private ServiceUserRepository serviceUserRepository;
    @Value("${emailer.guid}")
    private String guid;
    
    @Autowired
    public IdeaService(ServiceUserRepository serviceUserRepository, Emailer emailer) {
        this.logger = Logger.getLogger(IdeaService.class.getName());
        this.deck = null;
        this.emailer = emailer;
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
            this.user = newUser;
            this.user.setTodayCards(this.newCards());
            this.serviceUserRepository.save(newUser);
        }
        return this.user;
    }
    
    void saveIdea(String text, LocalDate now) {
        this.user.getDayIdeas().add(new Idea(text));
        this.user.setIdeasLastSubmitted(now);
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
    
    ArrayList<Idea> getCompleted() {
        return this.user.getCompleted();
    }
    
    void checkDate(LocalDate now) {
        if (user.getCardsUpdated().isBefore(now)) {
            this.updateStats(now);
            this.resetDayIdeas();
            user.setTodayCards(this.newCards());
            user.setCardsUpdated(now);
        }
        
        if (!user.getPastIdeas().isEmpty()) {
            user.getPastIdeas().removeIf(s -> s.getCreated().isBefore(now.minusDays(14)));
        }
        
        if (!user.getCompleted().isEmpty()) {
            user.getCompleted().removeIf(s -> s.getCompleted() != null && s.getCompleted().isBefore(now.minusDays(7)));
        }
        this.serviceUserRepository.save(user);
    }
    
    void setIdeaCompleted(int index) {
        this.getKeepers().get(index).setCompleted(LocalDate.now());
        
        this.getCompleted().add(this.getKeepers().get(index));
        this.getKeepers().remove(index);
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
        if (!user.getDayIdeas().isEmpty()) {
            user.getStats().setDaysUsed(user.getStats().getDaysUsed() + 1);
        }
        user.getDayIdeas().stream().forEach(s -> user.getPastIdeas().add(s));
        user.setDayIdeas(new ArrayList<>());
    }
    
    private void updateStats(LocalDate now) {
        
        if (this.user.getStats().getAverageDailyIdeas() == 0) {
            this.user.getStats().setAverageDailyIdeas(this.user.getDayIdeas().size());
        } else {
            this.user.getStats().setAverageDailyIdeas(((this.user.getStats().getAverageDailyIdeas() * (double) this.user.getStats().getDaysUsed()) + this.user.getDayIdeas().size()) / (this.user.getStats().getDaysUsed() + 1));
        }
        
        if (this.user.getIdeasLastSubmitted() == null || this.user.getIdeasLastSubmitted().isBefore(now.minusDays(1))) {
            this.user.getStats().setCurrentStreak(1);
        } else {
            this.user.getStats().setCurrentStreak(this.user.getStats().getCurrentStreak() + 1);
        }
        
        if (this.user.getStats().getCurrentStreak() > this.user.getStats().getMaxStreak()) {
            this.user.getStats().setMaxStreak(this.user.getStats().getCurrentStreak());
        }
    }
    
    String sendDailyEmail(String email, String name, double average, int maxStreak) {
        String subject = "It's time to get creative";
        String textPart = "How many ideas can you come up with today? Your current average is " + String.format("%.1f", average) + " ideas per day, and your longest streak so far is " + maxStreak + ".";
        String htmlPart = "<h3>Visit <a href='https://ideaengine-373522.lm.r.appspot.com/'>Idea Engine</a> to keep your streak going!</h3>";
        String customId = "IdeaEngineDaily";
        String response = "";
        try {
            response = String.valueOf(this.emailer.SendPrompt(email, name, subject, textPart, htmlPart, customId).getStatus());
        } catch (MailjetException ex) {
            this.logger.log(Level.SEVERE, null, ex);
        } catch (MailjetSocketTimeoutException ex) {
            this.logger.log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    String sendWeeklyEmail(String email, String name, int pastIdeasCount) {
        String subject = "Come check out your recent ideas";
        String textPart = "Everything starts from a good idea.";
        if (pastIdeasCount > 1) {
            textPart = textPart + " You've come up with " + pastIdeasCount + " ideas in the last two weeks.";
        }
        String htmlPart = "<h3>Visit <a href='https://ideaengine-373522.lm.r.appspot.com/'>Idea Engine</a> to review your recent ideas.</h3>";
        String customId = "IdeaEngineWeekly";
        String response = "";
        try {
            response = String.valueOf(this.emailer.SendPrompt(email, name, subject, textPart, htmlPart, customId).getStatus());
        } catch (MailjetException ex) {
            this.logger.log(Level.SEVERE, null, ex);
        } catch (MailjetSocketTimeoutException ex) {
            this.logger.log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    String runEmailer(String guid) throws IdeaException {
        if (!guid.equals(this.guid)) {
            throw new IdeaException("Invalid key");
        }
        List<ServiceUser> users = StreamSupport.stream(this.serviceUserRepository.findAll().spliterator(), false).collect(Collectors.toList());
        final StringBuffer responseLog = new StringBuffer();
        users.stream().forEach(s -> {
            if (s.isReceiveDailyPrompt()) {
                responseLog.append(s.getId()).append(" (Daily): ").append(this.sendDailyEmail(s.getEmail(), s.getName(), s.getStats().getAverageDailyIdeas(), s.getStats().getMaxStreak()));
            }
            if (s.isReceiveWeeklyPrompt() && LocalDate.now().getDayOfWeek() == DayOfWeek.SATURDAY) {
                responseLog.append(s.getId()).append(" (Weekly): ").append(this.sendWeeklyEmail(s.getEmail(), s.getName(), s.getPastIdeas().size()));
            }
        });
        
        this.logger.info("Messages sent: " + responseLog.toString());
        return responseLog.toString();
    }
    
    boolean deleteUser(OAuth2User principal) throws IdeaException {
        Optional<ServiceUser> found = this.serviceUserRepository.findByEmail(principal.getAttribute("email"));
        if (!found.isPresent()) {
            throw new IdeaException("User not found!");
        }
        this.serviceUserRepository.delete(found.get());
        return true;
    }
    
    Stats getStats() {
        Stats stats = new Stats(this.user.getStats().getDaysUsed(), this.user.getStats().getCurrentStreak(), this.user.getStats().getMaxStreak(), this.user.getStats().getAverageDailyIdeas());
        return stats;
    }
    
    Card[] getTodayCards() {
        Card[] cards = new Card[2];
        cards[0] = this.deck.getCardByIndex(this.user.getTodayCards()[0]);
        cards[1] = this.deck.getCardByIndex(this.user.getTodayCards()[1]);
        return cards;
    }
    
    void updateSettings(Settings settings) {
        this.getUser().setReceiveDailyPrompt(settings.isReceiveDailyPrompt());
        this.getUser().setReceiveWeeklyPrompt(settings.isReceiveWeeklyPrompt());
        this.serviceUserRepository.save(user);
    }
}
