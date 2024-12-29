package me.aydgn.mymusictracker.util;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import me.aydgn.mymusictracker.model.Song;

public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";
    private static final String DEFAULT_ALBUM_ART_URL = "https://raw.githubusercontent.com/aydgnme/MyMusicTracker/main/app/src/main/res/drawable/ic_album_placeholder.xml";

    public static void clearDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Veritabanı başarıyla temizlendi");
                initializeDatabase();
            })
            .addOnFailureListener(e -> Log.e(TAG, "Veritabanı temizlenirken hata: " + e.getMessage()));
    }

    public static void initializeDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        List<Song> songs = new ArrayList<>();

        // Örnek şarkıları ekle
        addTaylorSwiftAlbums(songs);
        addArianaGrandeAlbums(songs);
        addTheWeekndAlbums(songs);
        addMetallicaAlbums(songs);
        addIronMaidenAlbums(songs);
        addEminemAlbums(songs);
        addKendrickLamarAlbums(songs);
        addDaftPunkAlbums(songs);
        addAviciiAlbums(songs);
        addBeyonceAlbums(songs);
        addFrankOceanAlbums(songs);

        // Şarkıları Firebase'e yükle
        for (Song song : songs) {
            String songId = databaseRef.child("songs").push().getKey();
            if (songId != null) {
                databaseRef.child("songs").child(songId).setValue(song)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Şarkı eklendi: " + song.getTitle()))
                    .addOnFailureListener(e -> Log.e(TAG, "Şarkı eklenirken hata: " + e.getMessage()));
            }
        }
    }

    private static void addTaylorSwiftAlbums(List<Song> songs) {
        String[] titles = {
            "Shake It Off",
            "Blank Space",
            "Style",
            "Bad Blood",
            "Wildest Dreams"
        };
        addAlbum(songs, titles, "1989", "Taylor Swift", "Pop", 2014);
    }

    private static void addArianaGrandeAlbums(List<Song> songs) {
        String[] titles = {
            "thank u, next",
            "7 rings",
            "break up with your girlfriend, i'm bored",
            "NASA",
            "bloodline"
        };
        addAlbum(songs, titles, "thank u, next", "Ariana Grande", "Pop", 2019);
    }

    private static void addTheWeekndAlbums(List<Song> songs) {
        String[] titles = {
            "Blinding Lights",
            "Save Your Tears",
            "In Your Eyes",
            "Heartless",
            "After Hours"
        };
        addAlbum(songs, titles, "After Hours", "The Weeknd", "R&B", 2020);
    }

    private static void addMetallicaAlbums(List<Song> songs) {
        String[] titles = {
            "Enter Sandman",
            "Sad but True",
            "The Unforgiven",
            "Nothing Else Matters",
            "Wherever I May Roam"
        };
        addAlbum(songs, titles, "Metallica", "Metallica", "Metal", 1991);
    }

    private static void addIronMaidenAlbums(List<Song> songs) {
        String[] titles = {
            "The Trooper",
            "Run to the Hills",
            "The Number of the Beast",
            "Fear of the Dark",
            "Hallowed Be Thy Name"
        };
        addAlbum(songs, titles, "Best of the Beast", "Iron Maiden", "Metal", 1996);
    }

    private static void addEminemAlbums(List<Song> songs) {
        String[] titles = {
            "Lose Yourself",
            "The Real Slim Shady",
            "Stan",
            "Without Me",
            "Not Afraid"
        };
        addAlbum(songs, titles, "Curtain Call: The Hits", "Eminem", "Hip Hop", 2005);
    }

    private static void addKendrickLamarAlbums(List<Song> songs) {
        String[] titles = {
            "HUMBLE.",
            "DNA.",
            "LOYALTY.",
            "ELEMENT.",
            "LOVE."
        };
        addAlbum(songs, titles, "DAMN.", "Kendrick Lamar", "Hip Hop", 2017);
    }

    private static void addDaftPunkAlbums(List<Song> songs) {
        String[] titles = {
            "Get Lucky",
            "Instant Crush",
            "Lose Yourself to Dance",
            "Give Life Back to Music",
            "Doin' it Right"
        };
        addAlbum(songs, titles, "Random Access Memories", "Daft Punk", "Electronic", 2013);
    }

    private static void addAviciiAlbums(List<Song> songs) {
        String[] titles = {
            "Wake Me Up",
            "Hey Brother",
            "Addicted to You",
            "Levels",
            "Waiting For Love"
        };
        addAlbum(songs, titles, "True", "Avicii", "Electronic", 2013);
    }

    private static void addBeyonceAlbums(List<Song> songs) {
        String[] titles = {
            "Formation",
            "Sorry",
            "Hold Up",
            "Freedom",
            "All Night"
        };
        addAlbum(songs, titles, "Lemonade", "Beyoncé", "R&B", 2016);
    }

    private static void addFrankOceanAlbums(List<Song> songs) {
        String[] titles = {
            "Nikes",
            "Ivy",
            "Pink + White",
            "Solo",
            "Nights"
        };
        addAlbum(songs, titles, "Blonde", "Frank Ocean", "R&B", 2016);
    }

    private static void addAlbum(List<Song> songs, String[] titles, String album,
                               String artist, String genre, int year) {
        String albumId = album.toLowerCase().replace(" ", "_") + "_" + year;

        for (int i = 0; i < titles.length; i++) {
            Song song = new Song(null, titles[i], artist, album, albumId, genre, DEFAULT_ALBUM_ART_URL, i + 1, 180);
            songs.add(song);
        }
    }
} 