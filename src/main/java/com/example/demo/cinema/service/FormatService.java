package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Format;
import com.example.demo.cinema.repository.FormatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FormatService {

    private static final Logger log = LoggerFactory.getLogger(FormatService.class);
    private final FormatRepository formatRepository;

    @Autowired
    public FormatService(FormatRepository formatRepository) {
        this.formatRepository = formatRepository;
    }

    @Transactional(readOnly = true)
    public List<Format> getAllFormats() {
        return formatRepository.findAll();
    }

    @Transactional
    public Set<Format> findOrCreateFormatsByNames(List<String> formatNames) {
        if (formatNames == null || formatNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Format> resolvedFormats = new HashSet<>();
        List<String> trimmedNames = formatNames.stream().map(String::trim).filter(name -> !name.isEmpty()).distinct().collect(Collectors.toList());
        if (trimmedNames.isEmpty()) {
            return new HashSet<>();
        }
        List<Format> existingFormats = formatRepository.findByNameIn(trimmedNames);

        for (String name : trimmedNames) {
            Optional<Format> found = existingFormats.stream()
                                      .filter(f -> f.getName().equalsIgnoreCase(name))
                                      .findFirst();
            if (found.isPresent()) {
                resolvedFormats.add(found.get());
            } else {
                log.info("Format '{}' not found, creating new one.", name);
                Format newFormat = new Format(name);
                resolvedFormats.add(formatRepository.save(newFormat));
            }
        }
        return resolvedFormats;
    }

    public Optional<Format> findById(Long id) {
        if (id == null) return Optional.empty(); 
        return formatRepository.findById(id);
    }
}