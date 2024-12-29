package me.aydgn.mymusictracker;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import me.aydgn.mymusictracker.adapter.SongAdapter;
import me.aydgn.mymusictracker.model.Song;

public class PlaylistDetailActivity extends AppCompatActivity {
    private static final String TAG = "PlaylistDetailActivity";
    private String playlistId;
    private String playlistName;
    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        // Firebase başlat
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Intent'ten playlist bilgilerini al
        playlistId = getIntent().getStringExtra("playlist_id");
        playlistName = getIntent().getStringExtra("playlist_name");

        if (playlistId == null || playlistName == null) {
            Toast.makeText(this, "Playlist bulunamadı", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Toolbar'ı ayarla
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(playlistName);

        // RecyclerView'ı ayarla
        recyclerView = findViewById(R.id.songsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Şarkıları yükle
        loadPlaylistSongs();
    }

    private void loadPlaylistSongs() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference playlistSongsRef = databaseRef
            .child("playlists")
            .child(userId)
            .child(playlistId)
            .child("songs");

        playlistSongsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> songIds = new ArrayList<>();
                for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                    String songId = songSnapshot.getKey();
                    if (songId != null) {
                        // Firebase'deki özel karakterleri geri dönüştür
                        songId = songId.replaceAll("_", ".");
                        songIds.add(songId);
                    }
                }
                loadSongDetails(songIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Playlist şarkıları yüklenemedi", error.toException());
                Toast.makeText(PlaylistDetailActivity.this,
                    getString(R.string.error_loading_playlist, error.getMessage()),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSongDetails(List<String> songIds) {
        List<Song> songs = new ArrayList<>();
        DatabaseReference songsRef = databaseRef.child("songs");

        for (String songId : songIds) {
            songsRef.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Song song = snapshot.getValue(Song.class);
                    if (song != null) {
                        song.setId(snapshot.getKey());
                        songs.add(song);
                        adapter.updateSongs(songs);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Şarkı detayları yüklenemedi", error.toException());
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 