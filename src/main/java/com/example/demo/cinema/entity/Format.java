package com.example.demo.cinema.entity; 

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "formats")
public class Format {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public Format() {
    }

    public Format(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Format format = (Format) o;
        return Objects.equals(id, format.id) &&
               Objects.equals(name, format.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Format{" +
               "id=" + id +
               ", name='" + name + '\'' +
               '}';
    }
}