package com.example.demo.cinema.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.cinema.entity.SeatType;
import com.example.demo.cinema.service.SeatTypeService;

@Controller
@RequestMapping("admin/seat-types")
public class SeatTypeController {
    @Autowired
    private SeatTypeService seatTypeService;

    @GetMapping
    public String listSeatTypes(Model model, HttpServletRequest request) {
        model.addAttribute("seatTypes", seatTypeService.findAll());
        model.addAttribute("currentURI", request.getRequestURI());

        return "admin/seat-types/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, HttpServletRequest request) {
        model.addAttribute("seatType", new SeatType());
        model.addAttribute("currentURI", request.getRequestURI());
        return "admin/seat-types/form"; 
    }

    @PostMapping("/save")
    public String saveSeatType(@ModelAttribute SeatType seatType) {
        seatTypeService.save(seatType);
        return "redirect:/admin/seat-types";
    }

    @GetMapping("/edit/{id}")
    public String editSeatType(@PathVariable Long id, Model model, HttpServletRequest request) {
        SeatType seatType = seatTypeService.findById(id)
                                 .orElseThrow(() -> new IllegalArgumentException("Invalid seat type ID: " + id));
        model.addAttribute("seatType", seatType);
        model.addAttribute("currentURI", request.getRequestURI());
        return "admin/seat-types/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteSeatType(@PathVariable Long id) {
        seatTypeService.deleteById(id);
        return "redirect:/admin/seat-types";
    }
}