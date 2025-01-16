package me.aydgn.mymusictracker.model;

public class Album {
    private String id;
    private String title;
    private String artist;
    private String albumArtUrl;
    private long releaseDate;
    private String genre;

    public Album() {
        // Empty constructor for Firebase
    }

    public Album(String id, String title, String artist, String albumArtUrl, long releaseDate, String genre) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumArtUrl = albumArtUrl;
        this.releaseDate = releaseDate;
        this.genre = genre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
    }

    public long getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
} 