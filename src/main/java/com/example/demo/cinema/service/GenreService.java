package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Genre;
import com.example.demo.cinema.repository.GenreRepository;
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
public class GenreService {

    private static final Logger log = LoggerFactory.getLogger(GenreService.class);
    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Genre> findById(Long id) {
        return genreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Genre> findByName(String name) {
        return genreRepository.findByNameIgnoreCase(name);
    }

    @Transactional
    public Genre saveGenre(Genre genre) {
        Optional<Genre> existingGenre = genreRepository.findByNameIgnoreCase(genre.getName());
        if (existingGenre.isPresent()) {
            return existingGenre.get();
        }
        return genreRepository.save(genre);
    }

    @Transactional(readOnly = true)
    public Set<Genre> findOrCreateGenresByNames(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Genre> resolvedGenres = new HashSet<>();
        List<Genre> existingGenres = genreRepository.findByNameIn(
            genreNames.stream().map(String::trim).collect(Collectors.toList())
        );

        for (String name : genreNames) {
            String trimmedName = name.trim();
            Optional<Genre> found = existingGenres.stream()
                                      .filter(g -> g.getName().equalsIgnoreCase(trimmedName))
                                      .findFirst();
            if (found.isPresent()) {
                resolvedGenres.add(found.get());
            }
        }
        return resolvedGenres;
    }
}