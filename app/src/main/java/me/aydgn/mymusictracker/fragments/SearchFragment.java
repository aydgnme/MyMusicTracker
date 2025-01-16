package me.aydgn.mymusictracker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
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

public class SearchFragment extends Fragment {
    private TextInputEditText searchInput;
    private RecyclerView searchResultsRecyclerView;
    private TextView emptyStateText;
    private SongAdapter adapter;
    private DatabaseReference databaseRef;
    private List<Song> allSongs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        allSongs = new ArrayList<>();

        initializeViews(view);
        setupSearchInput();
        loadAllSongs();

        return view;
    }

    private void initializeViews(View view) {
        searchInput = view.findViewById(R.id.searchInput);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(getContext(), new ArrayList<>());
        searchResultsRecyclerView.setAdapter(adapter);
    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                filterSongs(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllSongs() {
        DatabaseReference albumsRef = databaseRef.child("albums");
        albumsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongs.clear();
                for (DataSnapshot albumSnapshot : snapshot.getChildren()) {
                    String albumTitle = albumSnapshot.child("title").getValue(String.class);
                    String artist = albumSnapshot.child("artist").getValue(String.class);
                    String coverUrl = albumSnapshot.child("coverUrl").getValue(String.class);

                    DataSnapshot songsSnapshot = albumSnapshot.child("songs");
                    for (DataSnapshot songSnapshot : songsSnapshot.getChildren()) {
                        String songId = songSnapshot.getKey();
                        String title = songSnapshot.child("title").getValue(String.class);
                        String duration = songSnapshot.child("duration").getValue(String.class);
                        Integer trackNumber = songSnapshot.child("trackNumber").getValue(Integer.class);

                        if (title != null && duration != null && trackNumber != null) {
                            Song song = new Song(songId, title, artist, albumTitle, coverUrl, duration, trackNumber);
                            allSongs.add(song);
                        }
                    }
                }
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "Error loading songs", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterSongs(String query) {
        List<Song> filteredSongs = new ArrayList<>();
        
        for (Song song : allSongs) {
            if (query.isEmpty() ||
                    song.getTitle().toLowerCase().contains(query) ||
                    song.getArtist().toLowerCase().contains(query) ||
                    song.getAlbum().toLowerCase().contains(query)) {
                filteredSongs.add(song);
            }
        }

        adapter.updateSongs(filteredSongs);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (searchInput.getText().toString().trim().isEmpty()) {
            emptyStateText.setText("Use the search bar above to search for music");
            emptyStateText.setVisibility(View.VISIBLE);
        } else if (adapter.getItemCount() == 0) {
            emptyStateText.setText("No results found");
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            emptyStateText.setVisibility(View.GONE);
        }
    }
} 