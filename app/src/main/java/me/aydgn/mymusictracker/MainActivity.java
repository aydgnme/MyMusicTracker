package me.aydgn.mymusictracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private RecyclerView recentlyPlayedRecyclerView;
    private RecyclerView topGenresRecyclerView;
    private RecyclerView allSongsRecyclerView;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase başlatma
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Toolbar ayarları
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // View'ları bağlama
        recentlyPlayedRecyclerView = findViewById(R.id.recentlyPlayedRecyclerView);
        topGenresRecyclerView = findViewById(R.id.topGenresRecyclerView);
        allSongsRecyclerView = findViewById(R.id.allSongsRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        fabAddSong = findViewById(R.id.fabAddSong);

        // RecyclerView ayarları
        setupRecyclerViews();

        // Bottom Navigation ayarları
        bottomNavigationView.setOnItemSelectedListener(this);

        // FAB click listener
        fabAddSong.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SongDetailActivity.class));
        });

        // Varsayılan olarak Home seçili
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void setupRecyclerViews() {
        // Recently Played RecyclerView
        recentlyPlayedRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Top Genres RecyclerView
        topGenresRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // All Songs RecyclerView
        allSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_home) {
            return true;
        } else if (itemId == R.id.navigation_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (itemId == R.id.navigation_library) {
            startActivity(new Intent(this, LibraryActivity.class));
            return true;
        } else if (itemId == R.id.navigation_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        
        return false;
    }
}