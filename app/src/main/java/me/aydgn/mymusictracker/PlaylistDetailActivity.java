package me.aydgn.mymusictracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.aydgn.mymusictracker.adapter.SongAdapter;
import me.aydgn.mymusictracker.model.Song;
import me.aydgn.mymusictracker.model.Playlist;

public class PlaylistDetailActivity extends AppCompatActivity {
    private static final String TAG = "PlaylistDetailActivity";
    private String playlistId;
    private RecyclerView songsRecyclerView;
    private SongAdapter adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private ShapeableImageView playlistCoverImage;
    private TextView playlistNameText;
    private TextView createdByText;
    private TextView songCountText;
    private Toolbar toolbar;
    private Playlist currentPlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Get playlist ID from Intent
        playlistId = getIntent().getStringExtra("playlist_id");
        if (playlistId == null) {
            Toast.makeText(this, "Playlist not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Load playlist details
        loadPlaylistDetails();

        // Load songs
        loadPlaylistSongs();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        playlistCoverImage = findViewById(R.id.playlistCoverImage);
        playlistNameText = findViewById(R.id.playlistNameText);
        createdByText = findViewById(R.id.createdByText);
        songCountText = findViewById(R.id.songCountText);
        songsRecyclerView = findViewById(R.id.songsRecyclerView);

        // Setup RecyclerView
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(this, new ArrayList<>());
        songsRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_delete_playlist) {
            showDeleteConfirmationDialog();
            return true;
        } else if (item.getItemId() == R.id.action_share_playlist) {
            sharePlaylist();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete this playlist?")
            .setPositiveButton("Delete", (dialog, which) -> deletePlaylist())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deletePlaylist() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference playlistRef = databaseRef.child("playlists").child(userId).child(playlistId);
        
        playlistRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Playlist deleted", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error deleting playlist", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadPlaylistDetails() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference playlistRef = databaseRef.child("playlists").child(userId).child(playlistId);

        playlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String coverUrl = snapshot.child("coverUrl").getValue(String.class);
                String createdBy = snapshot.child("createdBy").getValue(String.class);

                // Update UI
                playlistNameText.setText(name);
                createdByText.setText("Created by " + (createdBy != null ? createdBy : "Unknown"));

                // Load cover image
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    Glide.with(PlaylistDetailActivity.this)
                        .load(coverUrl)
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .into(playlistCoverImage);
                } else {
                    playlistCoverImage.setImageResource(R.drawable.default_album_art);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaylistDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlaylistSongs() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference playlistRef = databaseRef.child("playlists").child(userId).child(playlistId).child("songs");

        playlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Boolean> songIds = (Map<String, Boolean>) snapshot.getValue();

                if (songIds != null && !songIds.isEmpty()) {
                    songCountText.setText(songIds.size() + " songs");
                    
                    // Load all albums once
                    DatabaseReference albumsRef = databaseRef.child("albums");
                    albumsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot albumsSnapshot) {
                            List<Song> songs = new ArrayList<>();
                            
                            // Search for each song ID in albums
                            for (String songId : songIds.keySet()) {
                                // Check all albums
                                for (DataSnapshot albumSnapshot : albumsSnapshot.getChildren()) {
                                    DataSnapshot songsSnapshot = albumSnapshot.child("songs").child(songId);
                                    if (songsSnapshot.exists()) {
                                        String title = songsSnapshot.child("title").getValue(String.class);
                                        String duration = songsSnapshot.child("duration").getValue(String.class);
                                        Integer trackNumber = songsSnapshot.child("trackNumber").getValue(Integer.class);
                                        String artist = albumSnapshot.child("artist").getValue(String.class);
                                        String albumTitle = albumSnapshot.child("title").getValue(String.class);
                                        String coverUrl = albumSnapshot.child("coverUrl").getValue(String.class);

                                        if (title != null && duration != null && trackNumber != null) {
                                            Song song = new Song(songId, title, artist, albumTitle, coverUrl, duration, trackNumber);
                                            songs.add(song);
                                            break; // No need to check other albums after finding the song
                                        }
                                    }
                                }
                            }
                            
                            // Update adapter after all songs are loaded
                            adapter.updateSongs(songs);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error loading songs: " + error.getMessage());
                            Toast.makeText(PlaylistDetailActivity.this, "Error loading songs", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    songCountText.setText("0 songs");
                    adapter.updateSongs(new ArrayList<>());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaylistDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePlaylist() {
        if (currentPlaylist != null) {
            // Local storage
            // ... existing code ...
            
            // Cloud storage
            Playlist.saveToFirebase(currentPlaylist);
            
            Toast.makeText(this, "Playlist saved to cloud", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadPlaylistFromCloud() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            Playlist.getPlaylistsForUser(userId, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                        Playlist playlist = playlistSnapshot.getValue(Playlist.class);
                        if (playlist != null && playlist.getId().equals(currentPlaylist.getId())) {
                            updateUI(playlist);
                            break;
                        }
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PlaylistDetailActivity.this, 
                        "Error loading playlist: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylistFromCloud();
    }

    private void updateUI(Playlist playlist) {
        if (playlist == null) return;
        
        currentPlaylist = playlist;
        playlistNameText.setText(playlist.getName());
        createdByText.setText("Created by " + playlist.getCreatedBy());
        songCountText.setText(playlist.getSongCount() + " songs");
        
        // Playlist kapak resmini yükle
        String coverUrl = playlist.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                .load(coverUrl)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(playlistCoverImage);
        } else {
            playlistCoverImage.setImageResource(R.drawable.default_album_art);
        }
        
        // Şarkıları güncelle
        if (playlist.getSongs() != null) {
            loadPlaylistSongs();
        } else {
            adapter.updateSongs(new ArrayList<>());
        }
    }

    private void sharePlaylist() {
        if (currentPlaylist == null) return;

        StringBuilder shareText = new StringBuilder();
        shareText.append("Check out my playlist: ").append(currentPlaylist.getName()).append("\n\n");
        shareText.append("Songs:\n");

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference albumsRef = databaseRef.child("albums");
        
        albumsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot albumsSnapshot) {
                Map<String, Boolean> playlistSongs = currentPlaylist.getSongs();
                if (playlistSongs != null && !playlistSongs.isEmpty()) {
                    for (DataSnapshot albumSnapshot : albumsSnapshot.getChildren()) {
                        DataSnapshot songsSnapshot = albumSnapshot.child("songs");
                        String artist = albumSnapshot.child("artist").getValue(String.class);
                        
                        for (String songId : playlistSongs.keySet()) {
                            DataSnapshot songSnapshot = songsSnapshot.child(songId);
                            if (songSnapshot.exists()) {
                                String title = songSnapshot.child("title").getValue(String.class);
                                if (title != null && artist != null) {
                                    shareText.append("- ").append(title)
                                            .append(" by ").append(artist)
                                            .append("\n");
                                }
                            }
                        }
                    }
                }

                // Paylaşım intent'ini oluştur
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentPlaylist.getName());
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());

                // Paylaşım menüsünü göster
                startActivity(Intent.createChooser(shareIntent, "Share Playlist Via"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaylistDetailActivity.this, 
                    "Error sharing playlist: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
} 