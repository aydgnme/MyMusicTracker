package me.aydgn.mymusictracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.aydgn.mymusictracker.LoginActivity;
import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.adapter.PlaylistAdapter;
import me.aydgn.mymusictracker.adapter.SongAdapter;
import me.aydgn.mymusictracker.model.Playlist;
import me.aydgn.mymusictracker.model.Song;
import me.aydgn.mymusictracker.PlaylistDetailActivity;

public class ProfileFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener {
    private MaterialButton logoutButton;
    private MaterialButton settingsButton;
    private RecyclerView favoritesRecyclerView;
    private RecyclerView playlistsRecyclerView;
    private SongAdapter songAdapter;
    private PlaylistAdapter playlistAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String userId;
    private ValueEventListener favoritesListener;
    private ValueEventListener playlistsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();

        initializeViews(view);
        setupRecyclerViews();
        setupButtons();
        loadUserData();
        loadFavorites();
        loadPlaylists();

        return view;
    }

    private void initializeViews(View view) {
        logoutButton = view.findViewById(R.id.logoutButton);
        settingsButton = view.findViewById(R.id.settingsButton);
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView);
        playlistsRecyclerView = view.findViewById(R.id.playlistsRecyclerView);

        // Add click event to FAB
        view.findViewById(R.id.createPlaylistFab).setOnClickListener(v -> showCreatePlaylistDialog());
        
        // Add click event to settings button
        settingsButton.setOnClickListener(v -> showSettingsDialog());
    }

    private void setupRecyclerViews() {
        // Favorites RecyclerView
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(getContext(), new ArrayList<>());
        favoritesRecyclerView.setAdapter(songAdapter);

        // Playlists RecyclerView
        playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        playlistAdapter = new PlaylistAdapter(requireContext(), new ArrayList<>(), this);
        playlistsRecyclerView.setAdapter(playlistAdapter);
    }

    private void setupButtons() {
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            databaseRef.child("users").child(userId).child("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fullName = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load user information", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void loadFavorites() {
        DatabaseReference favoritesRef = databaseRef.child("users").child(userId).child("favorites");
        favoritesListener = favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Song> favoriteSongs = new ArrayList<>();
                Map<String, Boolean> favoriteIds = (Map<String, Boolean>) snapshot.getValue();
                
                if (favoriteIds == null || favoriteIds.isEmpty()) {
                    songAdapter.updateSongs(favoriteSongs);
                    return;
                }
                
                DatabaseReference albumsRef = databaseRef.child("albums");
                albumsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot albumsSnapshot) {
                        for (String songId : favoriteIds.keySet()) {
                            boolean songFound = false;
                            for (DataSnapshot albumSnapshot : albumsSnapshot.getChildren()) {
                                if (songFound) break;
                                
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
                                        favoriteSongs.add(song);
                                        songFound = true; // Found the song, no need to check other albums
                                    }
                                }
                            }
                        }
                        songAdapter.updateSongs(favoriteSongs);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load song details", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlaylists() {
        DatabaseReference playlistsRef = databaseRef.child("playlists").child(userId);
        playlistsListener = playlistsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Playlist> playlists = new ArrayList<>();
                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    String playlistId = playlistSnapshot.getKey();
                    String name = playlistSnapshot.child("name").getValue(String.class);
                    String createdBy = playlistSnapshot.child("createdBy").getValue(String.class);
                    String coverUrl = playlistSnapshot.child("coverUrl").getValue(String.class);
                    
                    if (name != null) {
                        Playlist playlist = new Playlist(playlistId, name, createdBy);
                        playlist.setCoverUrl(coverUrl);
                        
                        // Get songs as Map
                        DataSnapshot songsSnapshot = playlistSnapshot.child("songs");
                        if (songsSnapshot.exists()) {
                            Map<String, Boolean> songs = new HashMap<>();
                            for (DataSnapshot songSnapshot : songsSnapshot.getChildren()) {
                                songs.put(songSnapshot.getKey(), true);
                            }
                            playlist.setSongs(songs);
                        }
                        
                        playlists.add(playlist);
                    }
                }
                playlistAdapter.updatePlaylists(playlists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load playlists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onPlaylistClick(Playlist playlist) {
        Intent intent = new Intent(getContext(), PlaylistDetailActivity.class);
        intent.putExtra("playlist_id", playlist.getId());
        startActivity(intent);
    }

    @Override
    public void onPlaylistLongClick(Playlist playlist) {
        // Actions to perform when playlist is long-pressed
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favoritesListener != null) {
            databaseRef.child("users").child(userId).child("favorites").removeEventListener(favoritesListener);
        }
        if (playlistsListener != null) {
            databaseRef.child("playlists").child(userId).removeEventListener(playlistsListener);
        }
    }

    private void showCreatePlaylistDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_playlist, null);
        TextInputEditText playlistNameInput = dialogView.findViewById(R.id.playlistNameInput);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Playlist")
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

    private void createPlaylist(String name) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference playlistsRef = databaseRef.child("playlists").child(userId);
        String playlistId = playlistsRef.push().getKey();

        if (playlistId != null) {
            Map<String, Object> playlist = new HashMap<>();
            playlist.put("name", name);
            playlist.put("createdBy", userId);
            playlist.put("createdAt", System.currentTimeMillis());

            playlistsRef.child(playlistId).setValue(playlist)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Playlist created", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to create playlist", Toast.LENGTH_SHORT).show());
        }
    }

    private void showSettingsDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_settings, null);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        MaterialCheckBox[] genreCheckboxes = {
            dialogView.findViewById(R.id.rockCheckbox),
            dialogView.findViewById(R.id.popCheckbox),
            dialogView.findViewById(R.id.jazzCheckbox),
            dialogView.findViewById(R.id.classicalCheckbox),
            dialogView.findViewById(R.id.hipHopCheckbox)
        };

        // Load current user preferences
        databaseRef.child("users").child(userId).child("preferences").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot genresSnapshot = snapshot.child("genres");
                    if (genresSnapshot.exists()) {
                        Map<String, Boolean> genres = (Map<String, Boolean>) genresSnapshot.getValue();
                        if (genres != null) {
                            for (MaterialCheckBox checkbox : genreCheckboxes) {
                                String genre = checkbox.getText().toString().toLowerCase();
                                checkbox.setChecked(genres.containsKey(genre) && genres.get(genre));
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load preferences", Toast.LENGTH_SHORT).show();
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Settings")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                // Save password if changed
                String newPassword = passwordInput.getText().toString();
                if (!newPassword.isEmpty()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }

                // Save genre preferences
                Map<String, Boolean> selectedGenres = new HashMap<>();
                for (MaterialCheckBox checkbox : genreCheckboxes) {
                    if (checkbox.isChecked()) {
                        selectedGenres.put(checkbox.getText().toString().toLowerCase(), true);
                    }
                }
                
                databaseRef.child("users").child(userId).child("preferences").child("genres").setValue(selectedGenres)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Preferences saved successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save preferences", Toast.LENGTH_SHORT).show());
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
} 