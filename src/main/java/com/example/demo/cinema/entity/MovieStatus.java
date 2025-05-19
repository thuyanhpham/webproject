package com.example.demo.cinema.entity;

public enum MovieStatus {
    NOW_SHOWING("Now Showing"),
    COMING_SOON("Coming Soon"),
    ENDED("Ended"),
    HIDDEN("Hidden");

    private final String displayName;

    MovieStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MovieStatus[] publicValues() {
        return new MovieStatus[]{NOW_SHOWING, COMING_SOON}; 
    }
}
