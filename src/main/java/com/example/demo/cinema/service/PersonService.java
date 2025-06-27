package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Person;
import com.example.demo.cinema.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    @Autowired
    private PersonRepository personRepository;

    @Value("${app.upload.people.dir}")
    private String uploadDirForPeople;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadDirForPeople);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory for people!", e);
        }
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Person findById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with id: " + id));
    }

    @Transactional
    public void deleteById(Long id) {
        Person personToDelete = findById(id); 
        deleteAssociatedPhoto(personToDelete.getPhotoUrl());
        personRepository.deleteById(id);
    }

    @Transactional
    public Person save(Person person, MultipartFile photoFile) {
        if (person.getId() != null && photoFile != null && !photoFile.isEmpty()) {
            personRepository.findById(person.getId()).ifPresent(oldPerson -> {
                deleteAssociatedPhoto(oldPerson.getPhotoUrl());
            });
        }
        
        if (photoFile != null && !photoFile.isEmpty()) {
            String uniqueFileName = generateUniqueFileName(photoFile.getOriginalFilename());
            try {
                Files.copy(photoFile.getInputStream(), this.uploadPath.resolve(uniqueFileName));
                person.setPhotoUrl("/people-photos/" + uniqueFileName);
            } catch (IOException e) {
                throw new RuntimeException("Could not save photo file: " + e.getMessage());
            }
        }
        return personRepository.save(person);
    }

    private void deleteAssociatedPhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return; 
        }
        try {
            if (photoUrl.startsWith("/people-photos/")) {
                String fileName = photoUrl.substring("/people-photos/".length());
                Path photoPath = this.uploadPath.resolve(fileName);
                if (Files.exists(photoPath)) {
                    Files.delete(photoPath);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to delete photo file for URL {}: {}", photoUrl, e.getMessage());
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + fileExtension;
    }
}