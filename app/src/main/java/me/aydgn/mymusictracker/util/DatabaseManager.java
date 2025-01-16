package me.aydgn.mymusictracker.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.aydgn.mymusictracker.model.Album;
import me.aydgn.mymusictracker.model.Playlist;
import me.aydgn.mymusictracker.model.Song;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static DatabaseManager instance;
    private final FirebaseDatabase database;
    private final FirebaseAuth auth;
    private final DatabaseReference songsRef;
    private final DatabaseReference albumsRef;
    private final DatabaseReference playlistsRef;
    private final DatabaseReference usersRef;
    private final Map<String, Album> albumCache;

    private DatabaseManager() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        songsRef = database.getReference("songs");
        albumsRef = database.getReference("albums");
        playlistsRef = database.getReference("playlists");
        usersRef = database.getReference("users");
        albumCache = new HashMap<>();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public interface DataCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // Album
    public void getAlbum(String albumId, DataCallback<Album> callback) {
        if (albumCache.containsKey(albumId)) {
            callback.onSuccess(albumCache.get(albumId));
            return;
        }

        albumsRef.child(albumId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Album album = snapshot.getValue(Album.class);
                if (album != null) {
                    album.setId(snapshot.getKey());
                    albumCache.put(albumId, album);
                    callback.onSuccess(album);
                } else {
                    callback.onError("Album not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // Song
    public void getAllSongs(DataCallback<List<Song>> callback) {
        songsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Song> songs = new ArrayList<>();
                for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                    Song song = songSnapshot.getValue(Song.class);
                    if (song != null) {
                        song.setId(songSnapshot.getKey());
                        songs.add(song);
                    }
                }
                callback.onSuccess(songs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getSongsByAlbum(String albumId, DataCallback<List<Song>> callback) {
        songsRef.orderByChild("albumId").equalTo(albumId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Song> songs = new ArrayList<>();
                    for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                        Song song = songSnapshot.getValue(Song.class);
                        if (song != null) {
                            song.setId(songSnapshot.getKey());
                            songs.add(song);
                        }
                    }
                    callback.onSuccess(songs);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }

    // Playlist
    public void createPlaylist(String name, Song firstSong, DataCallback<String> callback) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userPlaylistsRef = playlistsRef.child(userId);
        String playlistId = userPlaylistsRef.push().getKey();

        if (playlistId != null) {
            Map<String, Object> playlist = new HashMap<>();
            playlist.put("name", name);
            playlist.put("createdAt", System.currentTimeMillis());

            String safeSongId = firstSong.getId().replaceAll("[.#$\\[\\]/]", "_");
            Map<String, Object> songs = new HashMap<>();
            songs.put(safeSongId, true);
            playlist.put("songs", songs);

            userPlaylistsRef.child(playlistId).setValue(playlist)
                .addOnSuccessListener(aVoid -> callback.onSuccess(playlistId))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onError("Failed to create playlist");
        }
    }

    public void getPlaylists(DataCallback<List<Playlist>> callback) {
        String userId = auth.getCurrentUser().getUid();
        playlistsRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Playlist> playlists = new ArrayList<>();
                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    Playlist playlist = playlistSnapshot.getValue(Playlist.class);
                    if (playlist != null) {
                        playlist.setId(playlistSnapshot.getKey());
                        playlists.add(playlist);
                    }
                }
                callback.onSuccess(playlists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void addSongToPlaylist(String playlistId, Song song, DataCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid();
        String safeSongId = song.getId().replaceAll("[.#$\\[\\]/]", "_");
        DatabaseReference playlistSongsRef = playlistsRef
            .child(userId)
            .child(playlistId)
            .child("songs");

        Map<String, Object> songUpdate = new HashMap<>();
        songUpdate.put(safeSongId, true);

        playlistSongsRef.updateChildren(songUpdate)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getPlaylistSongs(String playlistId, DataCallback<List<Song>> callback) {
        String userId = auth.getCurrentUser().getUid();
        playlistsRef.child(userId).child(playlistId).child("songs")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> songIds = new ArrayList<>();
                    for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                        String songId = songSnapshot.getKey();
                        if (songId != null) {
                            songId = songId.replaceAll("_", ".");
                            songIds.add(songId);
                        }
                    }
                    loadSongDetails(songIds, callback);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }

    private void loadSongDetails(List<String> songIds, DataCallback<List<Song>> callback) {
        List<Song> songs = new ArrayList<>();
        final int[] completedQueries = {0};
        final int totalQueries = songIds.size();

        if (totalQueries == 0) {
            callback.onSuccess(songs);
            return;
        }

        for (String songId : songIds) {
            songsRef.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Song song = snapshot.getValue(Song.class);
                    if (song != null) {
                        song.setId(snapshot.getKey());
                        songs.add(song);
                    }
                    completedQueries[0]++;
                    if (completedQueries[0] == totalQueries) {
                        callback.onSuccess(songs);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        }
    }

    // Favorites
    public void addToFavorites(Song song, DataCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference favoritesRef = usersRef.child(userId).child("favorites");
        Map<String, Object> favoriteUpdate = new HashMap<>();
        favoriteUpdate.put(song.getId(), true);

        favoritesRef.updateChildren(favoriteUpdate)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getFavorites(DataCallback<List<Song>> callback) {
        String userId = auth.getCurrentUser().getUid();
        usersRef.child(userId).child("favorites")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> songIds = new ArrayList<>();
                    for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                        if (Boolean.TRUE.equals(favoriteSnapshot.getValue(Boolean.class))) {
                            songIds.add(favoriteSnapshot.getKey());
                        }
                    }
                    loadSongDetails(songIds, callback);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }
} 