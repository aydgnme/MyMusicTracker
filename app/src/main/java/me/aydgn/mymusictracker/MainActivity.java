package me.aydgn.mymusictracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import me.aydgn.mymusictracker.fragments.FavoritesFragment;
import me.aydgn.mymusictracker.fragments.SongsFragment;
import me.aydgn.mymusictracker.fragments.ProfileFragment;
import me.aydgn.mymusictracker.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_songs) {
                selectedFragment = new SongsFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            //} else if (itemId == R.id.nav_favorites) {
                //selectedFragment = new FavoritesFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            }

            return true;
        });

        // Set initial fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SongsFragment())
                .commit();
        }
    }
}