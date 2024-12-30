package me.aydgn.mymusictracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.aydgn.mymusictracker.adapter.SongAdapter;
import me.aydgn.mymusictracker.model.Song;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recentlyPlayedRecyclerView;
    private RecyclerView allSongsRecyclerView;
    private SongAdapter recentlyPlayedAdapter;
    private SongAdapter allSongsAdapter;
    private DatabaseReference databaseRef;
    private BottomNavigationView bottomNavigation;
    private ExecutorService executorService;
    private Handler mainHandler;
    private List<Song> allSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Initialize threading components
        executorService = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
        allSongs = Collections.synchronizedList(new ArrayList<>());

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String toolbarTitle = getResources().getString(R.string.app_name);
        getSupportActionBar().setTitle(toolbarTitle);

        // Setup RecyclerViews
        setupRecyclerViews();

        // Setup bottom navigation
        setupBottomNavigation();

        // Adaptörleri başlat
        recentlyPlayedAdapter = new SongAdapter(this, new ArrayList<Song>(), song -> {
            Intent intent = new Intent(this, SongDetailActivity.class);
            intent.putExtra("song_id", song.getId());
            startActivity(intent);
        });

        allSongsAdapter = new SongAdapter(this, new ArrayList<Song>(), song -> {
            Intent intent = new Intent(this, SongDetailActivity.class);
            intent.putExtra("song_id", song.getId());
            startActivity(intent);
        });

        // Firebase'den şarkıları yükle
        loadSongs();
    }

    private void setupRecyclerViews() {
        // Recently Played
        recentlyPlayedRecyclerView = findViewById(R.id.recentlyPlayedRecyclerView);
        recentlyPlayedRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);

        // All Songs
        allSongsRecyclerView = findViewById(R.id.allSongsRecyclerView);
        allSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allSongsRecyclerView.setAdapter(allSongsAdapter);
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                startActivity(new Intent(this, LibraryActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadSongs() {
        databaseRef.child("songs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                executorService.execute(() -> {
                    List<Song> songs = new ArrayList<>();
                    for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                        try {
                            Song song = songSnapshot.getValue(Song.class);
                            if (song != null) {
                                song.setId(songSnapshot.getKey());
                                songs.add(song);
                            }
                        } catch (Exception e) {
                            Log.e("MainActivity", "Error parsing song: " + songSnapshot.getValue(), e);
                        }
                    }
                    
                    // UI güncellemelerini ana thread'de yap
                    mainHandler.post(() -> {
                        allSongs.clear();
                        allSongs.addAll(songs);
                        recentlyPlayedAdapter.updateSongs(new ArrayList<>(songs.subList(0, 
                            Math.min(songs.size(), 10))));
                        allSongsAdapter.updateSongs(songs);
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mainHandler.post(() -> {
                    String errorMessage = getString(R.string.error_message, error.getMessage());
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}