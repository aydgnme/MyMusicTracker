package me.aydgn.mymusictracker;

import android.app.Application;
import android.util.Log;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MyMusicTrackerApplication extends Application {
    private static final String TAG = "MyMusicTrackerApp";
    private static final boolean DEBUG = true; // Debug modunu manuel olarak ayarla

    @Override
    public void onCreate() {
        super.onCreate();
        setupPicasso();
    }

    private void setupPicasso() {
        // Önbellek dizinini oluştur
        File cacheDir = new File(getCacheDir(), "album_art");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // Picasso'yu yapılandır
        Picasso.Builder builder = new Picasso.Builder(this)
            .downloader(new OkHttp3Downloader(cacheDir, 250 * 1024 * 1024)); // 250MB önbellek

        // Hata ayıklama için log ekle
        if (DEBUG) {
            builder.listener((picasso, uri, exception) -> {
                Log.e(TAG, "Picasso yükleme hatası: " + uri.toString(), exception);
            });
        }

        try {
            Picasso picasso = builder.build();
            picasso.setIndicatorsEnabled(DEBUG);
            Picasso.setSingletonInstance(picasso);
        } catch (Exception e) {
            Log.e(TAG, "Picasso yapılandırma hatası", e);
        }
    }
} 