/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import com.nicksiepmann.ideaengine.domain.Card;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/")
    public String getDefaultPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        return "redirect:/today";
    }

    @GetMapping("/today")
    public String getToday(Model model, @AuthenticationPrincipal OAuth2User principal) {
        this.service.getUserFromDB(principal);
        model.addAttribute("cards", this.service.getTodayCards());
        model.addAttribute("todayideas", this.service.getDayIdeas());
        return "today";
    }

    @GetMapping("/ideas")
    public String getIdeas(Model model) {
        model.addAttribute("pastideas", this.service.getPastIdeas());
        model.addAttribute("keepers", this.service.getKeepers());
        return "ideas";
    }

    @GetMapping("/stats")
    public String getStats(Model model) {
        model.addAttribute("stats", this.service.getStats());
        return "ideas";
    }

    @GetMapping("/settings")
    public String getSettings(Model model) {
        model.addAttribute("senddaily", this.service.getUser().isReceiveDailyPrompt());
        model.addAttribute("sendweekly", this.service.getUser().isReceiveDailyPrompt());
        return "settings";
    }

    @PostMapping("/today")
    public String saveIdea(@RequestParam(value = "idea") String idea, Model model) {
        this.service.saveIdea(idea);
        return "today";
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam(value = "senddaily") String senddaily, @RequestParam(value = "sendweekly") String sendweekly, Model model) {
        this.service.getUser().setReceiveDailyPrompt(Boolean.valueOf(senddaily));
        this.service.getUser().setReceiveWeeklyPrompt(Boolean.valueOf(sendweekly));
        return "settings";
    }

    //submit idea changes and reload past and keepers
    //
    //Delete
    //delete account
}
