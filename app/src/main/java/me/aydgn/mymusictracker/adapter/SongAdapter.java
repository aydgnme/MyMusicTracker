package me.aydgn.mymusictracker.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import me.aydgn.mymusictracker.R;
import me.aydgn.mymusictracker.model.Song;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private static final String TAG = "SongAdapter";
    private final List<Song> songs;
    private final Context context;
    private final OnItemClickListener listener;
    private static final RequestOptions imageOptions = new RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .override(300, 300)
        .centerCrop();

    public interface OnItemClickListener {
        void onItemClick(Song song);
    }

    public SongAdapter(Context context, List<Song> songs, OnItemClickListener listener) {
        this.context = context;
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.titleTextView.setText(song.getTitle());
        holder.artistTextView.setText(song.getArtist());
        holder.albumTextView.setText(song.getAlbum());

        // Resmi Glide ile yükle ve önbellekle
        Log.d(TAG, "Loading album art from URL: " + song.getAlbumArtUrl());
        Glide.with(context)
            .load(song.getAlbumArtUrl())
            .apply(imageOptions)
            .placeholder(R.drawable.ic_album_placeholder)
            .error(R.drawable.ic_album_placeholder)
            .into(holder.albumArtImageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    public void updateSongs(List<Song> newSongs) {
        if (newSongs != null) {
            songs.clear();
            songs.addAll(newSongs);
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView artistTextView;
        public TextView albumTextView;
        public ShapeableImageView albumArtImageView;

        public ViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.titleTextView);
            artistTextView = view.findViewById(R.id.artistTextView);
            albumTextView = view.findViewById(R.id.albumTextView);
            albumArtImageView = view.findViewById(R.id.albumArtImageView);
        }
    }
} 