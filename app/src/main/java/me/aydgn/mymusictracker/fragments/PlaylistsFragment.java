package me.aydgn.mymusictracker.fragments;

import android.app.AlertDialog;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.adapter.PlaylistAdapter;
import me.aydgn.mymusictracker.model.Playlist;
import me.aydgn.mymusictracker.PlaylistDetailActivity;

public class PlaylistsFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener {
    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.playlistsRecyclerView);
        setupRecyclerView();

        // Add new playlist button
        FloatingActionButton fabAddPlaylist = view.findViewById(R.id.fabAddPlaylist);
        fabAddPlaylist.setOnClickListener(v -> showCreatePlaylistDialog());

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlaylistAdapter(requireContext(), new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void loadPlaylists() {
        String userId = mAuth.getCurrentUser().getUid();
        databaseRef.child("playlists").child(userId).addValueEventListener(new ValueEventListener() {
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
                adapter.updatePlaylists(playlists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load playlists: " + error.getMessage(),
                             Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreatePlaylistDialog() {
        // Create playlist dialog implementation
    }

    @Override
    public void onPlaylistClick(Playlist playlist) {
        Intent intent = new Intent(getContext(), PlaylistDetailActivity.class);
        intent.putExtra("playlist_id", playlist.getId());
        startActivity(intent);
    }

    @Override
    public void onPlaylistLongClick(Playlist playlist) {
        // Show delete confirmation dialog on long press
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete " + playlist.getName() + " playlist?")
            .setPositiveButton("Delete", (dialog, which) -> deletePlaylist(playlist))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deletePlaylist(Playlist playlist) {
        String userId = mAuth.getCurrentUser().getUid();
        databaseRef.child("playlists").child(userId).child(playlist.getId())
            .removeValue()
            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                "Playlist deleted", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(getContext(),
                "Failed to delete playlist: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
} 