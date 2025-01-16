package me.aydgn.mymusictracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.model.Playlist;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private Context context;
    private List<Playlist> playlists;
    private ItemTouchHelper touchHelper;
    private OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
        void onPlaylistLongClick(Playlist playlist);
    }

    public PlaylistAdapter(Context context, List<Playlist> playlists, OnPlaylistClickListener listener) {
        this.context = context;
        this.playlists = playlists;
        this.listener = listener;
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Playlist playlist = playlists.get(position);
                
                new AlertDialog.Builder(context)
                    .setTitle("Delete Playlist")
                    .setMessage("Are you sure you want to delete this playlist?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deletePlaylist(playlist, position);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        notifyItemChanged(position);
                    })
                    .show();
            }
        };
        
        touchHelper = new ItemTouchHelper(swipeCallback);
    }

    private void deletePlaylist(Playlist playlist, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference playlistRef = FirebaseDatabase.getInstance().getReference()
            .child("playlists")
            .child(userId)
            .child(playlist.getId());
            
        playlistRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                playlists.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                notifyItemChanged(position);
                Toast.makeText(context, "Error deleting playlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.bind(playlist);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void updatePlaylists(List<Playlist> newPlaylists) {
        this.playlists = newPlaylists;
        notifyDataSetChanged();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView coverImage;
        private final TextView nameText;
        private final TextView createdByText;
        private final TextView songCountText;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.playlistCoverImage);
            nameText = itemView.findViewById(R.id.playlistNameText);
            createdByText = itemView.findViewById(R.id.createdByText);
            songCountText = itemView.findViewById(R.id.songCountText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPlaylistClick(playlists.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPlaylistLongClick(playlists.get(position));
                    return true;
                }
                return false;
            });
        }

        void bind(Playlist playlist) {
            nameText.setText(playlist.getName());
            createdByText.setText("Created by " + playlist.getCreatedBy());
            songCountText.setText(playlist.getSongCount() + " songs");

            // Show playlist cover image if available, otherwise show default image
            if (playlist.getCoverUrl() != null && !playlist.getCoverUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(playlist.getCoverUrl())
                    .placeholder(R.drawable.default_album_art)
                    .error(R.drawable.default_album_art)
                    .into(coverImage);
            } else {
                coverImage.setImageResource(R.drawable.default_album_art);
            }
        }
    }
} 