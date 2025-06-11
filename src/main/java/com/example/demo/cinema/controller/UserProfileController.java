package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

// Import nếu bạn dùng @Valid cho validation
// import jakarta.validation.Valid;

// Import nếu bạn dùng LocalDate cho birthday
import java.time.LocalDate;

@Controller
public class UserProfileController {

    @Autowired
    private UserService userService;

    //private static final String UPLOAD_DIR = "E:/avamovie/avatars/";
    
    @Value("${app.upload.dir}") 
    private String uploadDir; 
    
    @GetMapping("/profile")
    public String viewUserProfile(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);

            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("currentRequestURI", "/profile");
            } else {
                model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng cho tài khoản này.");
                return "error-page";
            }
        } else {
            return "redirect:/sign-in";
        }
        return "user/userprofile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/sign-in";
        }

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
    public String updateProfile(/* @Valid */ @ModelAttribute("user") User user,
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

        
        existingUser.setFullname(user.getFullname());
        existingUser.setPhone(user.getPhone());
        existingUser.setGender(user.getGender());
        existingUser.setBirthday(user.getBirthday());

        
        if (!avatarFile.isEmpty()) {
            try {
                if (avatarFile.getSize() > 2 * 1024 * 1024) { // 2MB
                    redirectAttributes.addFlashAttribute("avatarError", "Kích thước ảnh tối đa là 2MB.");
                    redirectAttributes.addFlashAttribute("user", existingUser); 
                    return "redirect:/profile/edit";
                }

                Path uploadPath = Paths.get(uploadDir); 
                System.out.println("Attempting to create directory: " + uploadPath.toAbsolutePath());
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                    System.out.println("Directory created: " + uploadPath.toAbsolutePath());
                }else {
                    System.out.println("Directory already exists: " + uploadPath.toAbsolutePath());
                }

                String fileName = existingUser.getId() + "_" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                System.out.println("Attempting to save file to: " + filePath.toAbsolutePath());
                Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File saved successfully to: " + filePath.toAbsolutePath());
                
                existingUser.setAvatarUrl("/images/avatars/" + fileName);
                System.out.println("Avatar URL set to: " + existingUser.getAvatarUrl());

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error saving avatar: " + e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể tải lên ảnh đại diện.");
                redirectAttributes.addFlashAttribute("user", existingUser);
                return "redirect:/profile/edit"; 
            }
        }

        
        userService.save(existingUser); 

        
        User updatedUser = userService.findByUsername(authentication.getName()).orElse(null);
        if (updatedUser != null) {
            redirectAttributes.addFlashAttribute("user", updatedUser); 
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        return "redirect:/profile"; 
    }
}