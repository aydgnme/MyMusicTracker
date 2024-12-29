package me.aydgn.mymusictracker.model;

public class Song {
    private String id;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private int year;
    private boolean isFavorite;
    private long addedAt;
    private String userId;
    private String albumArtUrl;
    private String albumArtStoragePath;

    public Song() {
        // Empty constructor required for Firebase
    }

    public Song(String id, String title, String artist, String album, String genre, 
                int year, boolean isFavorite, long addedAt, String userId,
                String albumArtUrl, String albumArtStoragePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.year = year;
        this.isFavorite = isFavorite;
        this.addedAt = addedAt;
        this.userId = userId;
        this.albumArtUrl = albumArtUrl;
        this.albumArtStoragePath = albumArtStoragePath;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }
    public boolean isFavorite() { return isFavorite; }
    public long getAddedAt() { return addedAt; }
    public String getUserId() { return userId; }
    public String getAlbumArtUrl() { return albumArtUrl; }
    public String getAlbumArtStoragePath() { return albumArtStoragePath; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setYear(int year) { this.year = year; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setAlbumArtUrl(String albumArtUrl) { this.albumArtUrl = albumArtUrl; }
    public void setAlbumArtStoragePath(String albumArtStoragePath) { this.albumArtStoragePath = albumArtStoragePath; }
} 