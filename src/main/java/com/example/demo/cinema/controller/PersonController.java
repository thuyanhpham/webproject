package com.example.demo.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.cinema.entity.Person;
import com.example.demo.cinema.entity.PersonType;
import com.example.demo.cinema.service.PersonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/admin/people")
public class PersonController {

    @Autowired
    private PersonService personService;

    @ModelAttribute("personTypes")
    public PersonType[] getPersonTypes() {
        return PersonType.values();
    }

    @GetMapping
    public String listPeople(Model model) {
        model.addAttribute("people", personService.findAll());
        return "admin/people/list";
    }

    @GetMapping("/new")
    public String showNewPersonForm(Model model) {
        model.addAttribute("person", new Person());
        model.addAttribute("pageTiltle", "Add New Person");
        return "admin/people/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditPersonForm(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        try {
            Person person = personService.findById(id);
            model.addAttribute("person", person);
            model.addAttribute("pageTitle", "Edit Person (ID: " + id + ")");
            return "admin/people/form";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/people";
        }
    }
    
    @PostMapping("/save")
    public String savePerson(@ModelAttribute("person") Person person,
                             @RequestParam("photoFile") MultipartFile photoFile) {
        personService.save(person, photoFile);
        return "redirect:/admin/people";
    }
    
    @PostMapping("/delete/{id}")
    public String deletePerson(@PathVariable Long id) {
        personService.deleteById(id);
        return "redirect:/admin/people";
    }
}
