package me.aydgn.mymusictracker.model;

public class Playlist {
    private String id;
    private String name;

    public Playlist() {
        // Empty constructor required for Firebase
    }

    public Playlist(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
} 