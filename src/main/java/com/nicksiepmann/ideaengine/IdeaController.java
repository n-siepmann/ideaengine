/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.nicksiepmann.ideaengine;

import com.nicksiepmann.ideaengine.domain.Settings;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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

//    @GetMapping("/today")
//    public String tempLogin(Model model) {
//        this.service.getUserFromDB("Nick Siepmann", "**REMOVED**@gmail.com");
////        this.service.initialiseTestData();
//        model.addAttribute("cards", this.service.getTodayCards());
//        model.addAttribute("todayideas", this.service.getDayIdeas());
//        return "today";
//    }
    @GetMapping("/today")
    public String getToday(Model model, @AuthenticationPrincipal OAuth2User principal) {
        this.service.getUserFromDB(principal);
        model.addAttribute("cards", this.service.getTodayCards());
        model.addAttribute("todayideas", this.service.getDayIdeas());
        return "today";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/error")
    public String error(Model model, Exception ex) {
        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    @GetMapping("/")
    public String getDefaultPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        return "redirect:/today";
    }

    @GetMapping("/ideas")
    public String getIdeas(Model model) {
        model.addAttribute("pastideas", this.service.getPastIdeas());
        model.addAttribute("keepers", this.service.getKeepers());
        model.addAttribute("completedideas", this.service.getCompleted());
        return "ideas";
    }

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }

    @GetMapping("/stats")
    public String getStats(Model model) {
        model.addAttribute("stats", this.service.getStats());
        return "stats";
    }

    @GetMapping("/settings")
    public String getSettings(Model model) {
        Settings settings = new Settings(this.service.getUser().isReceiveDailyPrompt(), this.service.getUser().isReceiveWeeklyPrompt());
        model.addAttribute("settings", settings);
        return "settings";
    }

    @GetMapping("/run")
    public ResponseEntity<String> run(@RequestHeader("guid") String guid) {
        try {
            this.service.runEmailer(guid);
        } catch (IdeaException ex) {
            Logger.getLogger(IdeaController.class.getName()).log(Level.SEVERE, null, ex);
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/today")
    public String saveIdea(@RequestParam(value = "idea") String idea, Model model) {
        this.service.saveIdea(idea, LocalDate.now());
        return "redirect:/today";
    }

    @PostMapping("/settings")
    public String saveSettings(Model model, Settings settings) {
        this.service.updateSettings(settings);
        Settings newSettings = new Settings(this.service.getUser().isReceiveDailyPrompt(), this.service.getUser().isReceiveWeeklyPrompt());
        model.addAttribute("settings", newSettings);
        model.addAttribute("updated", Boolean.TRUE);
        return "settings";
    }

    @PostMapping("/keep")
    public String keepIdea(@RequestParam(value = "keepindex") String keepindex) {
        this.service.setIdeaKeeper(Integer.parseInt(keepindex));
        return "redirect:/ideas";
    }

    @PostMapping("/complete")
    public String completeIdea(@RequestParam(value = "completeindex") String completeindex, Model model) {
        this.service.setIdeaCompleted(Integer.parseInt(completeindex));
        return "redirect:/ideas";
    }

    @PostMapping("/deleteaccount")
    public String deleteAccount(HttpServletRequest request, Model model, @AuthenticationPrincipal OAuth2User principal) {
        try {
            this.service.deleteUser(principal);
        } catch (IdeaException ex) {
            Logger.getLogger(IdeaController.class.getName()).log(Level.SEVERE, null, ex);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @PostMapping("/deleteidea")
    public String deleteIdea(@RequestParam(value = "deleteindex") String deleteindex, Model model) {
        this.service.getKeepers().remove(this.service.getKeepers().get(Integer.parseInt(deleteindex)));
        return "redirect:/ideas";
    }

}
