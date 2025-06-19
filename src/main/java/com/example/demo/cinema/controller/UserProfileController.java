package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.service.UserService;
import com.example.demo.cinema.service.UserProfileService; 

import java.io.IOException; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserProfileController {

    @Autowired
    private UserService userService; 
    
    @Autowired 
    private UserProfileService userProfileService;

 
    //private String uploadDir;

    @GetMapping("/profile")
    public String viewUserProfile(Model model, Authentication authentication) {
        

        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("currentRequestURI", "/profile");
        } else {
            model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng cho tài khoản này.");
            return "error-page";
        }
        return "user/userprofile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByUsername(username).orElse(null);

        if (user == null) {
            model.addAttribute("errorMessage", "Không tìm thấy người dùng để chỉnh sửa.");
            return "error-page";
        }

        model.addAttribute("user", user);
        model.addAttribute("currentRequestURI", "/profile/edit");

        return "user/editprofile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile( @ModelAttribute("user") User user, 
                                BindingResult bindingResult,
                                @RequestParam("avatarFile") MultipartFile avatarFile,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {

        
        User existingUser = userService.findByUsername(authentication.getName()).orElse(null);
        if (existingUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng để cập nhật.");
            return "user/userprofile";
        }

       
        if (bindingResult.hasErrors()) {
            
            user.setUsername(existingUser.getUsername());
            user.setEmail(existingUser.getEmail());
            user.setCreatedAt(existingUser.getCreatedAt());
            user.setRole(existingUser.getRole());
            user.setAvatarUrl(existingUser.getAvatarUrl());

            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
            return "redirect:/profile/edit";
        }

        try {
            
            User updatedUserInDb = userProfileService.updateUserDetailsAndAvatar(user, avatarFile, existingUser);
        
            redirectAttributes.addFlashAttribute("user", updatedUserInDb);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            
        } catch (IOException e) {
           
            e.printStackTrace();
            System.err.println("Error updating profile or saving avatar: " + e.getMessage());
            
            String errorMessage = "Không thể cập nhật hồ sơ.";
            if (e.getMessage() != null && e.getMessage().contains("Kích thước ảnh tối đa là")) {
                errorMessage = e.getMessage(); 
            }

            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            redirectAttributes.addFlashAttribute("user", existingUser); 
            return "redirect:/profile/edit";
        }

        return "redirect:/profile";
    }
}