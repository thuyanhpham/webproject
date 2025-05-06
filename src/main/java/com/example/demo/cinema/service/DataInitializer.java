package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Role;
import com.example.demo.cinema.entity.User;
// import com.example.demo.cinema.entity.Status; // Import nếu dùng Status Enum
import com.example.demo.cinema.repository.RoleRepository;
import com.example.demo.cinema.repository.UserRepository;

// Import Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Quan trọng: Import Transactional

import java.time.LocalDateTime; // Import nếu dùng
import java.util.HashSet;       // Import nếu dùng
import java.util.Optional;
import java.util.Set;         // Import nếu dùng

@Component // Đánh dấu là Spring Bean
@Order(1)  // Ưu tiên chạy sớm nếu có nhiều CommandLineRunner
public class DataInitializer implements CommandLineRunner {

    // Khởi tạo Logger thủ công
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired // Constructor Injection
    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // <<< Đảm bảo toàn bộ run() chạy trong MỘT transaction >>>
    public void run(String... args) throws Exception {
        log.info("Starting data initialization (Single Transaction)...");
        try {
            // --- BƯỚC 1: TẠO/LẤY ROLES VÀ SAVE_AND_FLUSH KHI CẦN ---
            log.info("Checking and creating Roles...");
            // Sử dụng phương thức helper đã sửa lại, đảm bảo flush khi tạo mới
            Role adminRole = findOrCreateRoleAndFlush("ROLE_ADMIN");
            Role userRole = findOrCreateRoleAndFlush("ROLE_USER");

            // Kiểm tra Role ADMIN vì nó cần thiết cho bước sau
            if (adminRole == null || adminRole.getId() == null) {
                 throw new IllegalStateException("Could not create or find 'ROLE_ADMIN'. Halting initialization.");
            }
             // Kiểm tra Role USER (chỉ cảnh báo nếu không có)
             if (userRole == null || userRole.getId() == null) {
                 log.warn("Could not create or find 'ROLE_USER'. Skipping sample user creation if applicable.");
             } else {
                 // Tạo user thường nếu cần
                 // createSampleUserIfNotExist(userRole, "sampleuser");
             }
             log.info("Roles available: ADMIN (ID={}), USER (ID={})", adminRole.getId(), userRole != null ? userRole.getId() : "N/A");


            // --- BƯỚC 2: TẠO ADMIN USER (NẾU CHƯA CÓ) ---
            log.info("Checking and creating Admin User...");
            String adminUsername = "thuyanhne"; // Username của bạn
             // Truyền đối tượng Role đã được saveAndFlush (nếu mới tạo)
            createAdminUserIfNotExist(adminRole, adminUsername);

            log.info("Data initialization completed successfully (Single Transaction).");

        } catch (Exception e) {
            log.error("!!! CRITICAL ERROR DURING DATA INITIALIZATION !!!", e);
            // Ném lại lỗi để dừng ứng dụng nếu khởi tạo thất bại
            throw new RuntimeException("Data initialization failed.", e);
        }
    }

    /**
     * Tìm Role theo tên. Nếu không thấy, tạo mới, lưu và flush ngay lập tức.
     * Chạy trong transaction của phương thức gọi (run()).
     * @param roleName Tên Role (ví dụ: ROLE_ADMIN)
     * @return Đối tượng Role đã tồn tại hoặc vừa được tạo và flush, hoặc null nếu lỗi.
     */
    private Role findOrCreateRoleAndFlush(String roleName) {
        String normalizedRoleName = roleName.toUpperCase();
        Optional<Role> roleOpt = roleRepository.findByName(normalizedRoleName); // Dùng findByName

        if (roleOpt.isPresent()) {
            log.info("Role '{}' already exists with ID: {}", normalizedRoleName, roleOpt.get().getId());
            return roleOpt.get(); // Trả về đối tượng đã được quản lý
        } else {
            log.info("Creating new Role: {}", normalizedRoleName);
            Role newRole = new Role();
            newRole.setName(normalizedRoleName);
            try {
                // <<< SỬ DỤNG saveAndFlush ĐỂ GHI VÀ LẤY ID NGAY >>>
                Role savedRole = roleRepository.saveAndFlush(newRole);
                log.info("===> Saved and flushed Role '{}' with ID: {}", savedRole.getName(), savedRole.getId());
                if(savedRole.getId() == null){
                     log.error("!!! Error: ID for Role '{}' is still null after saveAndFlush!", savedRole.getName());
                     return null;
                }
                return savedRole; // Trả về Role vừa tạo và flush
            } catch (Exception e) {
                log.error("!!! Error saving or flushing Role '{}': {}", normalizedRoleName, e.getMessage(), e);
                return null;
            }
        }
    }

    /**
     * Tạo người dùng Admin mặc định nếu chưa tồn tại.
     * Chạy trong transaction của phương thức gọi (run()).
     * @param adminRole Đối tượng Role ADMIN đã được flush và có ID hợp lệ.
     * @param adminUsername Tên đăng nhập của admin.
     */
    private void createAdminUserIfNotExist(Role adminRole, String adminUsername) {
         if (adminRole == null || adminRole.getId() == null) {
             log.error("!!! Skipping admin user '{}' creation because admin Role is invalid.", adminUsername);
             return;
         }

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            log.info("Creating admin user: {}", adminUsername);
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode("246810")); // Đổi mật khẩu!
            adminUser.setEmail("thuyanhne@gmail.com");
            adminUser.setFullname("Thuy Anh");       // Tên của bạn
            adminUser.setRole(adminRole); // <<< Gán đối tượng Role đã được saveAndFlush
            // adminUser.setStatus(Status.ACTIVE); // Nếu có enum Status
            // createdAt/updatedAt dùng @PrePersist/@PreUpdate

            try {
                log.debug("===> Preparing to save admin user '{}' referencing Role ID: {}", adminUser.getUsername(), adminRole.getId());
                userRepository.save(adminUser); // Lưu User
                log.info("Successfully saved admin user '{}'.", adminUsername);
            } catch (Exception e) {
                log.error("!!! Error saving admin user '{}': {}", adminUsername, e.getMessage(), e);
                throw e; // Ném lại để rollback transaction chính của run()
            }
        } else {
            log.info("Admin user '{}' already exists.", adminUsername);
        }
    }

     // Phương thức tạo user thường (ví dụ, có thể bỏ comment nếu cần)
     /*
     private void createSampleUserIfNotExist(Role userRole, String sampleUsername) {
         if (userRole == null || userRole.getId() == null) {
             log.warn("Skipping sample user '{}' creation because USER Role is invalid.", sampleUsername);
             return;
         }
         if (userRepository.findByUsername(sampleUsername).isEmpty()) {
             log.info("Creating sample user: {}", sampleUsername);
             User sampleUser = new User();
             sampleUser.setUsername(sampleUsername);
             sampleUser.setPassword(passwordEncoder.encode("password")); // Mật khẩu mặc định
             sampleUser.setEmail("sample@example.com");
             sampleUser.setFullname("Sample User");
             sampleUser.setRole(userRole);
             try {
                 userRepository.save(sampleUser);
                 log.info("Successfully saved sample user '{}'.", sampleUsername);
             } catch (Exception e) {
                 log.error("!!! Error saving sample user '{}': {}", sampleUsername, e.getMessage(), e);
                 throw e;
             }
         } else {
             log.info("Sample user '{}' already exists.", sampleUsername);
         }
     }
     */
}