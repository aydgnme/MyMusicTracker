package me.aydgn.mymusictracker.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.model.Song;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private Context context;
    private List<Song> songs;
    private DatabaseReference databaseRef;
    private String userId;

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        
        holder.titleTextView.setText(song.getTitle());
        holder.artistTextView.setText(song.getArtist());
        holder.durationTextView.setText(song.getDuration());
        
        if (song.getAlbumCoverUrl() != null && !song.getAlbumCoverUrl().isEmpty()) {
            Picasso.get()
                .load(song.getAlbumCoverUrl())
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(holder.albumArtImageView);
        } else {
            holder.albumArtImageView.setImageResource(R.drawable.default_album_art);
        }

        // Check favorite status
        checkFavoriteStatus(song.getId(), holder.favoriteButton);

        // Click handler for favorite button
        holder.favoriteButton.setOnClickListener(v -> toggleFavorite(song, holder.favoriteButton));

        // Click handler for playlist button
        holder.playlistButton.setOnClickListener(v -> showPlaylistDialog(song));
    }

    private void checkFavoriteStatus(String songId, ImageButton favoriteButton) {
        databaseRef.child("users").child(userId).child("favorites").child(songId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        favoriteButton.setImageResource(R.drawable.ic_favorite);
                    } else {
                        favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SongAdapter", "Failed to check favorite status", error.toException());
                }
            });
    }

    private void toggleFavorite(Song song, ImageButton favoriteButton) {
        DatabaseReference favoriteRef = databaseRef.child("users").child(userId)
            .child("favorites").child(song.getId());

        favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Remove from favorites
                    favoriteRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                            Toast.makeText(context, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, 
                            R.string.error_adding_to_favorites, Toast.LENGTH_SHORT).show());
                } else {
                    // Add to favorites
                    favoriteRef.setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            favoriteButton.setImageResource(R.drawable.ic_favorite);
                            Toast.makeText(context, R.string.added_to_favorites, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, 
                            R.string.error_adding_to_favorites, Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, R.string.error_adding_to_favorites, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPlaylistDialog(Song song) {
        // Get current playlists first
        databaseRef.child("playlists").child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> playlistNames = new ArrayList<>();
                    List<String> playlistIds = new ArrayList<>();
                    
                    for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                        String playlistId = playlistSnapshot.getKey();
                        String playlistName = playlistSnapshot.child("name").getValue(String.class);
                        if (playlistName != null) {
                            playlistNames.add(playlistName);
                            playlistIds.add(playlistId);
                        }
                    }

                    // Show playlist selection dialog
                    String[] items = new String[playlistNames.size() + 1];
                    items[0] = context.getString(R.string.create_playlist);
                    for (int i = 0; i < playlistNames.size(); i++) {
                        items[i + 1] = playlistNames.get(i);
                    }

                    new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.select_playlist)
                        .setItems(items, (dialog, which) -> {
                            if (which == 0) {
                                // Create new playlist
                                showCreatePlaylistDialog(song);
                            } else {
                                // Add to existing playlist
                                String selectedPlaylistId = playlistIds.get(which - 1);
                                addSongToPlaylist(song, selectedPlaylistId);
                            }
                        })
                        .show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, R.string.error_adding_to_playlist, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showCreatePlaylistDialog(Song song) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_create_playlist, null);
        TextView playlistNameInput = view.findViewById(R.id.playlistNameInput);

        new MaterialAlertDialogBuilder(context)
            .setTitle(R.string.create_playlist)
            .setView(view)
            .setPositiveButton(R.string.create, (dialog, which) -> {
                String playlistName = playlistNameInput.getText().toString().trim();
                if (!playlistName.isEmpty()) {
                    createPlaylistAndAddSong(playlistName, song);
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void createPlaylistAndAddSong(String playlistName, Song song) {
        DatabaseReference newPlaylistRef = databaseRef.child("playlists").child(userId).push();
        String playlistId = newPlaylistRef.getKey();

        if (playlistId != null) {
            newPlaylistRef.child("name").setValue(playlistName)
                .addOnSuccessListener(aVoid -> addSongToPlaylist(song, playlistId))
                .addOnFailureListener(e -> Toast.makeText(context, 
                    R.string.error_adding_to_playlist, Toast.LENGTH_SHORT).show());
        }
    }

    private void addSongToPlaylist(Song song, String playlistId) {
        databaseRef.child("playlists").child(userId).child(playlistId)
            .child("songs").child(song.getId()).setValue(true)
            .addOnSuccessListener(aVoid -> Toast.makeText(context, 
                R.string.added_to_playlist, Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(context, 
                R.string.error_adding_to_playlist, Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView albumArtImageView;
        TextView titleTextView;
        TextView artistTextView;
        TextView durationTextView;
        ImageButton favoriteButton;
        ImageButton playlistButton;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArtImageView = itemView.findViewById(R.id.albumArtImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            playlistButton = itemView.findViewById(R.id.playlistButton);
        }
    }
} 