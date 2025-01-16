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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.adapter.SongAdapter;
import me.aydgn.mymusictracker.model.Song;
import me.aydgn.mymusictracker.SongDetailActivity;

public class FavoritesFragment extends Fragment {
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private DatabaseReference databaseRef;
    private String userId;
    private ValueEventListener favoritesListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(songAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        DatabaseReference favoritesRef = databaseRef.child("users").child(userId).child("favorites");
        favoritesListener = favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Song> favoriteSongs = new ArrayList<>();
                List<String> favoriteSongIds = new ArrayList<>();
                
                // Get favorite song IDs
                for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                    // Using songId directly as key
                    String songId = favoriteSnapshot.getKey();
                    if (songId != null) {
                        favoriteSongIds.add(songId);
                    }
                }
                
                if (favoriteSongIds.isEmpty()) {
                    songAdapter.updateSongs(favoriteSongs);
                    Toast.makeText(getContext(), "You don't have any favorite songs yet", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Get song details from albums collection
                databaseRef.child("albums").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot albumsSnapshot) {
                        for (DataSnapshot albumSnapshot : albumsSnapshot.getChildren()) {
                            String albumTitle = albumSnapshot.child("title").getValue(String.class);
                            String artist = albumSnapshot.child("artist").getValue(String.class);
                            String coverUrl = albumSnapshot.child("coverUrl").getValue(String.class);
                            
                            DataSnapshot songsSnapshot = albumSnapshot.child("songs");
                            for (DataSnapshot songSnapshot : songsSnapshot.getChildren()) {
                                String songId = songSnapshot.getKey();
                                if (favoriteSongIds.contains(songId)) {
                                    String title = songSnapshot.child("title").getValue(String.class);
                                    String duration = songSnapshot.child("duration").getValue(String.class);
                                    Integer trackNumber = songSnapshot.child("trackNumber").getValue(Integer.class);
                                    
                                    if (title != null && duration != null && trackNumber != null) {
                                        Song song = new Song(songId, title, artist, albumTitle, coverUrl, duration, trackNumber);
                                        favoriteSongs.add(song);
                                    }
                                }
                            }
                        }
                        songAdapter.updateSongs(favoriteSongs);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error loading song details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading favorites: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favoritesListener != null) {
            databaseRef.child("users").child(userId).child("favorites").removeEventListener(favoritesListener);
        }
    }
} 