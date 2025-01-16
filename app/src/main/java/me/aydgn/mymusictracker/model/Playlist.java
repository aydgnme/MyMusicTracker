package me.aydgn.mymusictracker.model;

import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Exclude;

/**
 * Playlist sınıfı, müzik çalma listelerini temsil eder.
 * Bu sınıf hem yerel depolama hem de Firebase cloud depolama için kullanılır.
 */
public class Playlist {
    private String id;
    private String name;
    private String createdBy;
    private String coverUrl;
    private String description;
    private long createdAt;
    private String userId;
    private Map<String, Boolean> songs;

    public Playlist() {
        // Empty constructor for Firebase
        songs = new HashMap<>();
        createdAt = System.currentTimeMillis();
    }

    public Playlist(String id, String name, String userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.songs = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Map<String, Boolean> getSongs() {
        return songs;
    }

    public void setSongs(Map<String, Boolean> songs) {
        this.songs = songs;
    }

    public int getSongCount() {
        return songs != null ? songs.size() : 0;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Playlist nesnesini Firebase Realtime Database formatına dönüştürür
     * @return Map olarak playlist verileri
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("description", description);
        result.put("createdAt", createdAt);
        result.put("userId", userId);
        result.put("songs", songs);
        return result;
    }

    /**
     * Playlist'i Firebase Realtime Database'e kaydeder
     * @param playlist Kaydedilecek playlist
     */
    public static void saveToFirebase(Playlist playlist) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("playlists").push().getKey();
        Map<String, Object> playlistValues = playlist.toMap();
        
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/playlists/" + key, playlistValues);
        childUpdates.put("/user-playlists/" + playlist.getUserId() + "/" + key, playlistValues);
        
        mDatabase.updateChildren(childUpdates);
    }

    /**
     * Belirli bir kullanıcının playlistlerini Firebase'den getirir
     * @param userId Kullanıcı ID'si
     * @param listener Verileri dinleyen listener
     */
    public static void getPlaylistsForUser(String userId, ValueEventListener listener) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user-playlists").child(userId).addValueEventListener(listener);
    }
} 