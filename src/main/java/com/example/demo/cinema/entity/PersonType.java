package com.example.demo.cinema.entity;

public enum PersonType {

    ACTOR("Diễn viên"),
    DIRECTOR("Đạo diễn"),
    BOTH("Cả hai vai trò"),
    OTHER("Khác");

    private final String displayName;

    PersonType(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
