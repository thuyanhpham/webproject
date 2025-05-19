package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Format;
import com.example.demo.cinema.entity.Genre;
import com.example.demo.cinema.entity.Role;
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.FormatRepository;
import com.example.demo.cinema.repository.GenreRepository;
import com.example.demo.cinema.repository.RoleRepository;
import com.example.demo.cinema.repository.RoomRepository;
import com.example.demo.cinema.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final GenreRepository genreRepository;
    private final FormatRepository formatRepository;

    @Autowired 
    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoomRepository roomRepository,
                           GenreRepository genreRepository,
                           FormatRepository formatRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roomRepository = roomRepository;
        this.genreRepository = genreRepository;
        this.formatRepository = formatRepository;
    }

    @Override
    @Transactional 
    public void run(String... args) throws Exception {
        try {
            Role adminRole = findOrCreateRoleAndFlush("ROLE_ADMIN");
            Role userRole = findOrCreateRoleAndFlush("ROLE_USER");

            if (adminRole == null || adminRole.getId() == null) {
                 throw new IllegalStateException("Could not create or find 'ROLE_ADMIN'. Halting initialization.");
            }
             if (userRole == null || userRole.getId() == null) {
             }
             
            String adminUsername = "thuyanhne";
            createAdminUserIfNotExist(adminRole, adminUsername);
            
            //Tạo Room
            findOrCreateRoom("Phòng 1", 100);
            findOrCreateRoom("Phòng 2", 120);
            findOrCreateRoom("Phòng 3", 148);
            findOrCreateRoom("Phòng 4", 89);
            findOrCreateRoom("Phòng 5", 83);
            findOrCreateRoom("Phòng 6 (Inactive)", 80, false);
            
            //Tạo Genre
            createGenreIfNotExist("Action");
            createGenreIfNotExist("Adventure");
            createGenreIfNotExist("Animation");
            createGenreIfNotExist("Comedy");
            createGenreIfNotExist("Crime");
            createGenreIfNotExist("Documentary");
            createGenreIfNotExist("Drama");
            createGenreIfNotExist("Family");
            createGenreIfNotExist("Fantasy");
            createGenreIfNotExist("Healing");
            createGenreIfNotExist("History");
            createGenreIfNotExist("Horror");
            createGenreIfNotExist("Musical");
            createGenreIfNotExist("Mystery");
            createGenreIfNotExist("Romance");
            createGenreIfNotExist("Sci-Fi");
            createGenreIfNotExist("TV Movie");
            createGenreIfNotExist("Thriller");
            createGenreIfNotExist("War");
            createGenreIfNotExist("Western");
            
            //Tạo Format
            createFormatIfNotExist("2D");
            createFormatIfNotExist("3D");
            createFormatIfNotExist("IMAX");
            createFormatIfNotExist("4DX");
            
        } catch (Exception e) {
            log.error("!!! CRITICAL ERROR DURING DATA INITIALIZATION !!!", e);
            throw new RuntimeException("Data initialization failed.", e);
        }
        
    }

    private Room findOrCreateRoom(String roomName, int capacity) {
    	return findOrCreateRoom(roomName, capacity, true);
    }
    
    private Room findOrCreateRoom(String roomName, int capacity, boolean isActive) {
    	Optional<Room> roomOpt = roomRepository.findByName(roomName);
    	if (roomOpt.isPresent()) {
    		return roomOpt.get();
    	} else {
    		Room newRoom = new Room();
    		newRoom.setName(roomName);
    		newRoom.setCapacity(capacity);
    		newRoom.setActive(isActive);
    		
    		try {
    			Room savedRoom = roomRepository.save(newRoom);
    			return savedRoom;
    		} catch (Exception e) {
    			log.error("!!! Error saving Room '{}': {}", roomName, e.getMessage(), e);
    			return null;
    		}
    	}
    }
    
    private Genre createGenreIfNotExist(String name) {
        Optional<Genre> genreOpt = genreRepository.findByNameIgnoreCase(name);
        if (genreOpt.isEmpty()) {
            Genre newGenre = new Genre(name);
            return genreRepository.save(newGenre);
        } else {
            return genreOpt.get();
        }
    }
    
    private Format createFormatIfNotExist(String name) {
        Optional<Format> formatOpt = formatRepository.findByNameIgnoreCase(name);
        if (formatOpt.isEmpty()) {
            log.info("Creating format: {}", name);
            Format newFormat = new Format(name);
            return formatRepository.save(newFormat);
        }
        return formatOpt.get();
    }
    
    private Role findOrCreateRoleAndFlush(String roleName) {
        String normalizedRoleName = roleName.toUpperCase();
        Optional<Role> roleOpt = roleRepository.findByName(normalizedRoleName);

        if (roleOpt.isPresent()) {
            return roleOpt.get();
        } else {
            Role newRole = new Role();
            newRole.setName(normalizedRoleName);
            try {
                Role savedRole = roleRepository.saveAndFlush(newRole);
                if(savedRole.getId() == null){
                     log.error("!!! Error: ID for Role '{}' is still null after saveAndFlush!", savedRole.getName());
                     return null;
                }
                return savedRole;
            } catch (Exception e) {
                log.error("!!! Error saving or flushing Role '{}': {}", normalizedRoleName, e.getMessage(), e);
                return null;
            }
        }
    }

    private void createAdminUserIfNotExist(Role adminRole, String adminUsername) {
         if (adminRole == null || adminRole.getId() == null) {
             return;
         }

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode("246810"));
            adminUser.setEmail("thuyanhne@gmail.com");
            adminUser.setFullname("Thuy Anh");
            adminUser.setRole(adminRole); 

            try {
                userRepository.save(adminUser);
            } catch (Exception e) {
                log.error("!!! Error saving admin user '{}': {}", adminUsername, e.getMessage(), e);
                throw e; 
            }
        } 
    }
}