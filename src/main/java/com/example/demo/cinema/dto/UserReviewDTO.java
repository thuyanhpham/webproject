package com.example.demo.cinema.dto;

public class UserReviewDTO {
	 	private String username;
	    private String fullname;
	    private String avatarUrl;

	    public UserReviewDTO() {
	    }

	    public UserReviewDTO(String username, String fullname, String avatarUrl) {
	        this.username = username;
	        this.fullname = fullname;
	        this.avatarUrl = avatarUrl;
	    }

	    public String getUsername() {
	        return username;
	    }

	    public void setUsername(String username) {
	        this.username = username;
	    }

	    public String getFullname() {
	        return fullname;
	    }

	    public void setFullname(String fullname) {
	        this.fullname = fullname;
	    }

	    public String getAvatarUrl() {
	        return avatarUrl;
	    }

	    public void setAvatarUrl(String avatarUrl) {
	        this.avatarUrl = avatarUrl;
	    }
}
