package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Role;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.RoleRepository;
import com.example.demo.cinema.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // QUAN TRỌNG
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Bắt đầu khởi tạo dữ liệu...");

        // --- BƯỚC 1: TẠO/LẤY ROLES ---
        log.info("Kiểm tra và tạo Roles...");
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    log.info("Tạo Role: ROLE_ADMIN");
                    Role newAdminRole = new Role();
                    newAdminRole.setName("ROLE_ADMIN");
                    Role savedRole = roleRepository.save(newAdminRole);
                    entityManager.flush();
                    log.info("===> Đã LƯU Role mới 'ROLE_ADMIN' với ID: {}", savedRole.getId());
                    return savedRole;
                });

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                     log.info("Tạo Role: ROLE_USER");
                     Role newUserRole = new Role();
                     newUserRole.setName("ROLE_USER");
                     Role savedRole = roleRepository.save(newUserRole);
                     entityManager.flush();
                     log.info("===> Đã LƯU Role mới 'ROLE_USER' với ID: {}", savedRole.getId());
                     return savedRole;
                 });

        log.info("Đã tạo hoặc lấy xong Roles.");

        // *** Đảm bảo adminRole hợp lệ trước khi tiếp tục ***
        if (adminRole == null || adminRole.getId() == null) {
            log.error("!!! LỖI NGHIÊM TRỌNG: Không thể lấy hoặc tạo Role 'ROLE_ADMIN'. Dừng khởi tạo User.");
            throw new RuntimeException("Không thể khởi tạo Role 'ROLE_ADMIN' cần thiết.");
        }
         log.info("===> Role ADMIN sẽ được dùng có ID: {}", adminRole.getId());


        // --- BƯỚC 2: TẠO ADMIN USER (NẾU CHƯA CÓ) ---
        log.info("Kiểm tra và tạo Admin User...");
        String adminUsername = "thuyanhne";

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            log.info("Tạo admin user: {}", adminUsername);
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            // !!! QUAN TRỌNG: Mã hóa mật khẩu !!!
            adminUser.setPassword(passwordEncoder.encode("246810"));
            adminUser.setEmail("thuyanhne@gmail.com");
            adminUser.setFullname("Administrator");
            // Set các thuộc tính khác nếu cần
            // adminUser.setStatus(Status.ACTIVE); // Trạng thái hoạt động

            // *** QUAN TRỌNG: Gán đúng Role ADMIN đã lấy/tạo ở trên ***
            log.info("===> Gán Role (ID: {}) cho user '{}'", adminRole.getId(), adminUsername);
            adminUser.setRole(adminRole);

            try {
                 log.info("===> Chuẩn bị LƯU user '{}' với Role ID: {}", adminUser.getUsername(), (adminUser.getRole() != null ? adminUser.getRole().getId() : "NULL"));
                 userRepository.save(adminUser);
                 log.info("Lưu admin user '{}' thành công.", adminUsername);
            } catch (Exception e) {
                 // Log lỗi và ném lại để transaction rollback
                 log.error("!!! Lỗi khi lưu admin user '{}'. User data: {}. Error: {}", adminUsername, adminUser, e.getMessage(), e);
                 throw e; // Ném lại lỗi
            }
        } else {
            log.info("Admin user '{}' đã tồn tại.", adminUsername);
        }

        // Có thể thêm code tạo user thường ở đây nếu cần

        log.info("Hoàn tất khởi tạo dữ liệu.");
    }
}