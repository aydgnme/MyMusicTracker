package me.aydgn.mymusictracker;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.aydgn.mymusictracker.model.Playlist;
import me.aydgn.mymusictracker.model.Song;

public class SongDetailActivity extends AppCompatActivity {
    private static final String TAG = "SongDetailActivity";
    private String songId;
    private Song currentSong;
    private DatabaseReference songsRef;
    private DatabaseReference usersRef;
    private String userId;
    private ShapeableImageView albumArtImageView;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        songsRef = databaseRef.child("songs");
        usersRef = databaseRef.child("users");
        userId = mAuth.getCurrentUser().getUid();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Song Details");

        // Initialize views
        albumArtImageView = findViewById(R.id.albumArtImageView);

        // Get song ID from intent
        songId = getIntent().getStringExtra("song_id");
        if (songId == null) {
            Toast.makeText(this, "Song not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load song details
        loadSongDetails();

        // Setup button click listeners
        MaterialButton addToFavoritesButton = findViewById(R.id.addToFavoritesButton);
        MaterialButton createPlaylistButton = findViewById(R.id.createPlaylistButton);
        MaterialButton addToPlaylistButton = findViewById(R.id.addToPlaylistButton);

        addToFavoritesButton.setOnClickListener(v -> addToFavorites());
        createPlaylistButton.setOnClickListener(v -> showCreatePlaylistDialog());
        addToPlaylistButton.setOnClickListener(v -> showPlaylistSelectionDialog());
    }

    private void loadSongDetails() {
        songsRef.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentSong = snapshot.getValue(Song.class);
                if (currentSong != null) {
                    currentSong.setId(snapshot.getKey());
                    updateUI(currentSong);
                } else {
                    Toast.makeText(SongDetailActivity.this, "Song not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SongDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Song song) {
        ((android.widget.TextView) findViewById(R.id.titleTextView)).setText(song.getTitle());
        ((android.widget.TextView) findViewById(R.id.artistTextView)).setText(song.getArtist());
        ((android.widget.TextView) findViewById(R.id.albumTextView)).setText(song.getAlbum());
        ((android.widget.TextView) findViewById(R.id.genreTextView)).setText(song.getGenre());

        // Load album art with logging
        if (song.getAlbumArtUrl() != null && !song.getAlbumArtUrl().isEmpty()) {
            Log.d(TAG, "Loading album art from URL: " + song.getAlbumArtUrl());
            Picasso.get()
                .load(song.getAlbumArtUrl())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_album_placeholder)
                .error(R.drawable.ic_album_placeholder)
                .into(albumArtImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully loaded album art");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to load album art: " + e.getMessage());
                        Toast.makeText(SongDetailActivity.this,
                            getString(R.string.error_message, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                        albumArtImageView.setImageResource(R.drawable.ic_album_placeholder);
                    }
                });
        } else {
            Log.d(TAG, "No album art URL available");
            albumArtImageView.setImageResource(R.drawable.ic_album_placeholder);
        }
    }

    private void addToFavorites() {
        if (currentSong == null) return;

        DatabaseReference favoritesRef = usersRef.child(userId).child("favorites");
        Map<String, Object> favoriteUpdate = new HashMap<>();
        favoriteUpdate.put(songId, true);

        favoritesRef.updateChildren(favoriteUpdate)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showCreatePlaylistDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_playlist, null);
        TextInputEditText playlistNameInput = dialogView.findViewById(R.id.playlistNameInput);

        new MaterialAlertDialogBuilder(this)
            .setTitle("Create Playlist")
            .setView(dialogView)
            .setPositiveButton("Create", (dialog, which) -> {
                String playlistName = playlistNameInput.getText().toString().trim();
                if (!playlistName.isEmpty()) {
                    createPlaylist(playlistName);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void createPlaylist(String playlistName) {
        if (currentSong == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference playlistsRef = databaseRef.child("playlists").child(userId);
        String playlistId = playlistsRef.push().getKey();

        if (playlistId != null) {
            Map<String, Object> playlist = new HashMap<>();
            playlist.put("name", playlistName);
            playlist.put("createdAt", System.currentTimeMillis());

            // Güvenli şarkı ID'si oluştur
            String safeSongId = currentSong.getId().replaceAll("[.#$\\[\\]/]", "_");
            Map<String, Object> songs = new HashMap<>();
            songs.put(safeSongId, true);
            playlist.put("songs", songs);

            playlistsRef.child(playlistId).setValue(playlist)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, R.string.success_playlist_created, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_message, e.getMessage()), Toast.LENGTH_SHORT).show());
        }
    }

    private void showPlaylistSelectionDialog() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference playlistsRef = databaseRef.child("playlists").child(userId);

        playlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Playlist> playlists = new ArrayList<>();
                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    String playlistId = playlistSnapshot.getKey();
                    String name = playlistSnapshot.child("name").getValue(String.class);
                    if (name != null) {
                        playlists.add(new Playlist(playlistId, name));
                    }
                }

                if (playlists.isEmpty()) {
                    Toast.makeText(SongDetailActivity.this, "Create a playlist first", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] playlistNames = new String[playlists.size()];
                for (int i = 0; i < playlists.size(); i++) {
                    playlistNames[i] = playlists.get(i).getName();
                }

                new MaterialAlertDialogBuilder(SongDetailActivity.this)
                    .setTitle("Select Playlist")
                    .setItems(playlistNames, (dialog, which) -> {
                        Playlist selectedPlaylist = playlists.get(which);
                        addSongToPlaylist(selectedPlaylist.getId());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SongDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSongToPlaylist(String playlistId) {
        if (currentSong == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        String safeSongId = currentSong.getId().replaceAll("[.#$\\[\\]/]", "_");
        DatabaseReference playlistSongsRef = databaseRef.child("playlists")
            .child(userId)
            .child(playlistId)
            .child("songs");

        Map<String, Object> songUpdate = new HashMap<>();
        songUpdate.put(safeSongId, true);

        playlistSongsRef.updateChildren(songUpdate)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Song added to playlist", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 