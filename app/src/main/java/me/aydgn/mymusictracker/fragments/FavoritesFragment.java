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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(getContext(), new ArrayList<>(), song -> {
            Intent intent = new Intent(getContext(), SongDetailActivity.class);
            intent.putExtra("song_id", song.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(songAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        databaseRef.child("users").child(userId).child("favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> favoriteIds = new ArrayList<>();
                        for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                            if (Boolean.TRUE.equals(favoriteSnapshot.getValue(Boolean.class))) {
                                favoriteIds.add(favoriteSnapshot.getKey());
                            }
                        }
                        loadSongs(favoriteIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Hata: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadSongs(List<String> songIds) {
        List<Song> songs = new ArrayList<>();
        DatabaseReference songsRef = databaseRef.child("songs");

        for (String songId : songIds) {
            songsRef.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Song song = snapshot.getValue(Song.class);
                    if (song != null) {
                        songs.add(song);
                        songAdapter.updateSongs(songs);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Hata: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
} 