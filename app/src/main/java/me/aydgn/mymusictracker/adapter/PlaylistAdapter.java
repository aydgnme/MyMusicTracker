package me.aydgn.mymusictracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.model.Playlist;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private List<Playlist> playlists;
    private OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
        void onPlaylistLongClick(Playlist playlist);
    }

    public PlaylistAdapter(List<Playlist> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
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