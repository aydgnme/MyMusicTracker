package me.aydgn.mymusictracker.model;

public class Song {
    private String id;
    private String title;
    private String artist;
    private String album;
    private String albumCoverUrl;
    private String duration;
    private int trackNumber;
    private String genre;

    // Empty constructor required for Firebase
    public Song() {
    }

    public Song(String id, String title, String artist, String album, String albumCoverUrl, String duration, int trackNumber) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumCoverUrl = albumCoverUrl;
        this.duration = duration;
        this.trackNumber = trackNumber;
        this.genre = genre;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumCoverUrl() {
        return albumCoverUrl;
    }

    public String getDuration() {
        return duration;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public String getGenre() {
        return genre;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setAlbumCoverUrl(String albumCoverUrl) {
        this.albumCoverUrl = albumCoverUrl;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
} 