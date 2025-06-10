package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

// Import nếu bạn dùng @Valid cho validation
// import jakarta.validation.Valid;

// Import nếu bạn dùng LocalDate cho birthday
import java.time.LocalDate;

@Controller
public class UserProfileController {

    @Autowired
    private UserService userService;

    private static final String UPLOAD_DIR = "E:/avamovie/avatars/";

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
            return "user/userprofile"; // Nên redirect về trang profile
        }

        // Xử lý lỗi validation (nếu có)
        // Nếu có lỗi, bạn nên trả về form editprofile và flash các lỗi
        if (bindingResult.hasErrors()) {
            // Đảm bảo các trường không sửa được vẫn có giá trị khi trả về form
            user.setUsername(existingUser.getUsername());
            user.setEmail(existingUser.getEmail());
            user.setCreatedAt(existingUser.getCreatedAt());
            user.setRole(existingUser.getRole());
            user.setAvatarUrl(existingUser.getAvatarUrl());

            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
            return "redirect:/profile/edit"; // Redirect về form chỉnh sửa với lỗi
        }

        // Cập nhật các trường được phép chỉnh sửa từ form vào existingUser
        existingUser.setFullname(user.getFullname());
        existingUser.setPhone(user.getPhone());
        existingUser.setGender(user.getGender());
        existingUser.setBirthday(user.getBirthday());

        // Xử lý upload avatar
        if (!avatarFile.isEmpty()) {
            try {
                if (avatarFile.getSize() > 2 * 1024 * 1024) { // 2MB
                    redirectAttributes.addFlashAttribute("avatarError", "Kích thước ảnh tối đa là 2MB.");
                    redirectAttributes.addFlashAttribute("user", existingUser); // Giữ dữ liệu hiện tại
                    return "redirect:/profile/edit";
                }

                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = existingUser.getId() + "_" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                existingUser.setAvatarUrl("/images/avatars/" + fileName);

            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể tải lên ảnh đại diện.");
                redirectAttributes.addFlashAttribute("user", existingUser);
                return "redirect:/profile/edit"; // Redirect về form chỉnh sửa với lỗi upload
            }
        }

        // Lưu người dùng đã cập nhật
        userService.save(existingUser); // Sử dụng save() của UserService để cập nhật

        // ****** ĐIỀU CHỈNH QUAN TRỌNG NHẤT TẠI ĐÂY ******
        // Sau khi lưu thành công, lấy lại đối tượng user từ DB để đảm bảo nó có tất cả các trường
        // bao gồm cả các trường không được gửi từ form như createdAt, email, username, v.v.
        // Đây cũng là cách tốt nhất để đảm bảo dữ liệu là mới nhất từ DB.
        User updatedUser = userService.findByUsername(authentication.getName()).orElse(null);
        if (updatedUser != null) {
            redirectAttributes.addFlashAttribute("user", updatedUser); // Chuyển đối tượng đã được cập nhật từ DB
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        return "redirect:/profile"; // Redirect về trang hiển thị profile
    }
}