package me.aydgn.mymusictracker.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String fullName;
    private String username;
    private String email;
    private List<String> favoriteGenres;
    private String profileImageUrl;

    public User() {
        // Empty constructor required for Firebase
        favoriteGenres = new ArrayList<>();
    }

    public User(String fullName, String username, String email, List<String> favoriteGenres) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.favoriteGenres = favoriteGenres != null ? favoriteGenres : new ArrayList<>();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getFavoriteGenres() {
        return favoriteGenres;
    }

    public void setFavoriteGenres(List<String> favoriteGenres) {
        this.favoriteGenres = favoriteGenres != null ? favoriteGenres : new ArrayList<>();
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
} 