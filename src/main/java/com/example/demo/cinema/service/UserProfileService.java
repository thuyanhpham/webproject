package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.UserRepository; // Cần UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate; 
@Service
public class UserProfileService {

    @Autowired
    private UserRepository userRepository; 
    
    @Value("${app.upload.dir}")
    private String uploadDir;

   
    public User updateUserDetailsAndAvatar(User userFromForm, MultipartFile avatarFile, User existingUser) throws IOException {
        
        existingUser.setFullname(userFromForm.getFullname());
        existingUser.setPhone(userFromForm.getPhone());
        existingUser.setGender(userFromForm.getGender());
        existingUser.setBirthday(userFromForm.getBirthday());

        
        if (!avatarFile.isEmpty()) {
            
            if (avatarFile.getSize() > 2 * 1024 * 1024) { // 2MB
                
                throw new IOException("Kích thước ảnh tối đa là 2MB.");
            }

           
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            
            String fileName = existingUser.getId() + "_" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            
            existingUser.setAvatarUrl("/images/avatars/" + fileName);
        }

        
        return userRepository.save(existingUser);
    }

    
}
