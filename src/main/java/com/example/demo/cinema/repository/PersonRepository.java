package com.example.demo.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.cinema.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

}
