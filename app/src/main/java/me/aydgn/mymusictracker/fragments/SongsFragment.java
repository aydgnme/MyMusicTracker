package me.aydgn.mymusictracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.adapter.SongAdapter;
import me.aydgn.mymusictracker.model.Song;
import me.aydgn.mymusictracker.SongDetailActivity;

public class SongsFragment extends Fragment {
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private DatabaseReference databaseRef;
    private TextInputLayout genreFilterLayout;
    private AutoCompleteTextView genreFilterInput;
    private List<Song> allSongs = new ArrayList<>();
    private ValueEventListener songsListener;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_songs, container, false);

        recyclerView = view.findViewById(R.id.songsRecyclerView);
        genreFilterLayout = view.findViewById(R.id.genreFilterLayout);
        genreFilterInput = view.findViewById(R.id.genreFilterInput);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference().child("albums");
        loadSongs();

        setupGenreFilter();

        return view;
    }

    private void setupGenreFilter() {
        String[] genres = {"All", "Pop", "Rock", "Progressive Rock", "Grunge"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            requireContext(),
            R.layout.dropdown_item,
            R.id.text_view_item,
            genres
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setBackgroundColor(getResources().getColor(R.color.spotify_dark_gray, null));
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(getResources().getColor(R.color.spotify_dark_gray, null));
                return view;
            }
        };
        
        genreFilterInput.setAdapter(adapter);
        genreFilterInput.setText("All", false);
        
        genreFilterInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGenre = adapter.getItem(position);
            filterSongs(selectedGenre);
        });
    }

    private void loadSongs() {
        songsListener = databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongs.clear();
                Set<String> genres = new HashSet<>();
                
                for (DataSnapshot albumSnapshot : snapshot.getChildren()) {
                    String albumTitle = albumSnapshot.child("title").getValue(String.class);
                    String artist = albumSnapshot.child("artist").getValue(String.class);
                    String coverUrl = albumSnapshot.child("coverUrl").getValue(String.class);
                    String genre = albumSnapshot.child("genre").getValue(String.class);
                    
                    if (genre != null) {
                        genres.add(genre);
                    }
                    
                    DataSnapshot songsSnapshot = albumSnapshot.child("songs");
                    for (DataSnapshot songSnapshot : songsSnapshot.getChildren()) {
                        String songId = songSnapshot.getKey();
                        String title = songSnapshot.child("title").getValue(String.class);
                        String duration = songSnapshot.child("duration").getValue(String.class);
                        Integer trackNumber = songSnapshot.child("trackNumber").getValue(Integer.class);
                        
                        if (title != null && duration != null && trackNumber != null) {
                            Song song = new Song(songId, title, artist, albumTitle, coverUrl, duration, trackNumber);
                            song.setGenre(genre);
                            allSongs.add(song);
                            Log.d("SongsFragment", "Added song: " + songId + " - " + title + " - Genre: " + genre);
                        }
                    }
                }
                
                // Set up adapter for genre filter
                List<String> genreList = new ArrayList<>(genres);
                genreList.add(0, "All");
                ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                    requireContext(),
                    R.layout.dropdown_item,
                    R.id.text_view_item,
                    genreList
                ) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        view.setBackgroundColor(getResources().getColor(R.color.spotify_dark_gray, null));
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        view.setBackgroundColor(getResources().getColor(R.color.spotify_dark_gray, null));
                        return view;
                    }
                };
                
                if (genreFilterInput != null) {
                    genreFilterInput.setAdapter(genreAdapter);
                }
                
                filterSongs("All"); // Show all songs initially
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Veri yüklenirken hata oluştu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSongs(String genre) {
        List<Song> filteredSongs = new ArrayList<>();
        if (genre.equals("All")) {
            filteredSongs.addAll(allSongs);
        } else {
            for (Song song : allSongs) {
                if (song.getGenre() != null && song.getGenre().equals(genre)) {
                    filteredSongs.add(song);
                }
            }
        }
        adapter.updateSongs(filteredSongs);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (songsListener != null) {
            databaseRef.removeEventListener(songsListener);
        }
    }
} 