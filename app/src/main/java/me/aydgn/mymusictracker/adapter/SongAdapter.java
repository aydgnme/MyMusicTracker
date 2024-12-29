package me.aydgn.mymusictracker.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.SongDetailActivity;
import me.aydgn.mymusictracker.model.Song;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private static final String TAG = "SongAdapter";
    private List<Song> songs;
    private static final Map<String, String> albumArtCache = new HashMap<>();

    public SongAdapter(List<Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView artistTextView;
        private final ShapeableImageView albumArtImageView;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            artistTextView = itemView.findViewById(R.id.artistTextView);
            albumArtImageView = itemView.findViewById(R.id.albumArtImageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Song song = songs.get(position);
                    Intent intent = new Intent(itemView.getContext(), SongDetailActivity.class);
                    intent.putExtra("song_id", song.getId());
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        void bind(Song song) {
            titleTextView.setText(song.getTitle());
            artistTextView.setText(song.getArtist());

            // Albüm kapağını yükle
            loadAlbumArt(song);
        }

        private void loadAlbumArt(Song song) {
            String albumKey = song.getArtist() + "_" + song.getAlbum();
            String cachedUrl = albumArtCache.get(albumKey);

            if (cachedUrl != null) {
                // Önbellekteki URL'yi kullan
                loadImageWithPicasso(cachedUrl);
            } else if (song.getAlbumArtUrl() != null && !song.getAlbumArtUrl().isEmpty()) {
                // Yeni URL'yi önbelleğe al ve kullan
                albumArtCache.put(albumKey, song.getAlbumArtUrl());
                loadImageWithPicasso(song.getAlbumArtUrl());
            } else {
                // Varsayılan resmi göster
                albumArtImageView.setImageResource(R.drawable.ic_album_placeholder);
            }
        }

        private void loadImageWithPicasso(String url) {
            Log.d(TAG, "Loading album art from URL: " + url);
            Picasso.get()
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.ic_album_placeholder)
                .error(R.drawable.ic_album_placeholder)
                .fit()
                .centerCrop()
                .into(albumArtImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully loaded album art from cache");
                    }

                    @Override
                    public void onError(Exception e) {
                        // Önbellekte yoksa internetten yükle
                        Picasso.get()
                            .load(url)
                            .placeholder(R.drawable.ic_album_placeholder)
                            .error(R.drawable.ic_album_placeholder)
                            .fit()
                            .centerCrop()
                            .into(albumArtImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Successfully loaded album art from network");
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e(TAG, "Failed to load album art: " + e.getMessage());
                                }
                            });
                    }
                });
        }
    }
} 