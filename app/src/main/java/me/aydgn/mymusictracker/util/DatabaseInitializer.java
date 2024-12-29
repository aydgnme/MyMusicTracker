package me.aydgn.mymusictracker.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import me.aydgn.mymusictracker.model.Song;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        List<Song> songs = new ArrayList<>();

        // Twenty One Pilots albümleri
        addBlurryfaceAlbum(songs);
        addVesselAlbum(songs);
        addTrenchAlbum(songs);
        addScaledAndIcyAlbum(songs);
        addSelfTitledAlbum(songs);
        addRegionalAtBestAlbum(songs);

        // Şarkıları veritabanına ekle
        for (Song song : songs) {
            // Benzersiz bir ID oluştur
            String songId = databaseRef.child("songs").push().getKey();
            if (songId != null) {
                song.setId(songId);
                databaseRef.child("songs").child(songId).setValue(song);
            }
        }
    }

    private static void addBlurryfaceAlbum(List<Song> songs) {
        String[] titles = {
            "Heavydirtysoul", "Stressed Out", "Ride", "Fairly Local",
            "Tear in My Heart", "Lane Boy", "The Judge", "Doubt",
            "Polarize", "We Don't Believe What's on TV", "Message Man",
            "Hometown", "Not Today", "Goner"
        };
        
        String album = "Blurryface";
        String artist = "Twenty One Pilots";
        String genre = "Alternative";
        int year = 2015;
        long timestamp = System.currentTimeMillis();
        String userId = "system";
        String albumArtUrl = "https://firebasestorage.googleapis.com/v0/b/mymusictracker.appspot.com/o/albums%2Fblurryface.jpg";
        String albumArtStoragePath = "albums/blurryface.jpg";

        for (String title : titles) {
            Song song = new Song(null, title, artist, album, genre, year, false, 
                               timestamp, userId, albumArtUrl, albumArtStoragePath);
            songs.add(song);
        }
    }

    private static void addVesselAlbum(List<Song> songs) {
        String[] titles = {
            "Ode to Sleep", "Holding on to You", "Migraine", "House of Gold",
            "Car Radio", "Semi-Automatic", "Screen", "The Run and Go",
            "Fake You Out", "Guns for Hands", "Trees", "Truce"
        };
        
        String album = "Vessel";
        String artist = "Twenty One Pilots";
        String genre = "Alternative";
        int year = 2013;
        long timestamp = System.currentTimeMillis();
        String userId = "system";
        String albumArtUrl = "https://firebasestorage.googleapis.com/v0/b/mymusictracker.appspot.com/o/albums%2Fvessel.jpg";
        String albumArtStoragePath = "albums/vessel.jpg";

        for (String title : titles) {
            Song song = new Song(null, title, artist, album, genre, year, false, 
                               timestamp, userId, albumArtUrl, albumArtStoragePath);
            songs.add(song);
        }
    }

    private static void addTrenchAlbum(List<Song> songs) {
        String[] titles = {
            "Jumpsuit", "Levitate", "Morph", "My Blood", "Chlorine",
            "Smithereens", "Neon Gravestones", "The Hype", "Nico and the Niners",
            "Cut My Lip", "Bandito", "Pet Cheetah", "Legend", "Leave the City"
        };
        
        String album = "Trench";
        String artist = "Twenty One Pilots";
        String genre = "Alternative";
        int year = 2018;
        long timestamp = System.currentTimeMillis();
        String userId = "system";
        String albumArtUrl = "https://firebasestorage.googleapis.com/v0/b/mymusictracker.appspot.com/o/albums%2Ftrench.jpg";
        String albumArtStoragePath = "albums/trench.jpg";

        for (String title : titles) {
            Song song = new Song(null, title, artist, album, genre, year, false, 
                               timestamp, userId, albumArtUrl, albumArtStoragePath);
            songs.add(song);
        }
    }

    private static void addScaledAndIcyAlbum(List<Song> songs) {
        String[] titles = {
            "Good Day", "Choker", "Shy Away", "The Outside", "Saturday",
            "Never Take It", "Mulberry Street", "Formidable", "Bounce Man",
            "No Chances", "Redecorate"
        };
        
        String album = "Scaled and Icy";
        String artist = "Twenty One Pilots";
        String genre = "Alternative";
        int year = 2021;
        long timestamp = System.currentTimeMillis();
        String userId = "system";
        String albumArtUrl = "https://firebasestorage.googleapis.com/v0/b/mymusictracker.appspot.com/o/albums%2Fscaled_and_icy.jpg";
        String albumArtStoragePath = "albums/scaled_and_icy.jpg";

        for (String title : titles) {
            Song song = new Song(null, title, artist, album, genre, year, false, 
                               timestamp, userId, albumArtUrl, albumArtStoragePath);
            songs.add(song);
        }
    }

    private static void addSelfTitledAlbum(List<Song> songs) {
        String[] titles = {
            "Implicit Demand for Proof", "Fall Away", "The Pantaloon",
            "Addict with a Pen", "Friend, Please", "March to the Sea",
            "Johnny Boy", "Oh Ms Believer", "Air Catcher", "Trapdoor",
            "A Car, a Torch, a Death", "Taxi Cab", "Before You Start Your Day",
            "Isle of Flightless Birds"
        };
        
        String album = "Twenty One Pilots";
        String artist = "Twenty One Pilots";
        String genre = "Alternative";
        int year = 2009;
        long timestamp = System.currentTimeMillis();
        String userId = "system";
        String albumArtUrl = "https://firebasestorage.googleapis.com/v0/b/mymusictracker.appspot.com/o/albums%2Fself_titled.jpg";
        String albumArtStoragePath = "albums/self_titled.jpg";

        for (String title : titles) {
            Song song = new Song(null, title, artist, album, genre, year, false, 
                               timestamp, userId, albumArtUrl, albumArtStoragePath);
            songs.add(song);
        }
    }

    private static void addRegionalAtBestAlbum(List<Song> songs) {
        String[] titles = {
            "Guns for Hands", "Holding on to You", "Ode to Sleep", "Slowtown",
            "Car Radio", "Forest", "Glowing Eyes", "Kitchen Sink", "Anathema",
            "Lovely", "Ruby", "Trees", "Be Concerned", "Clear"
        };
        
        String album = "Regional at Best";
        String artist = "Twenty One Pilots";
        String genre = "Alternative";
        int year = 2011;
        long timestamp = System.currentTimeMillis();
        String userId = "system";
        String albumArtUrl = "https://firebasestorage.googleapis.com/v0/b/mymusictracker.appspot.com/o/albums%2Fregional_at_best.jpg";
        String albumArtStoragePath = "albums/regional_at_best.jpg";

        for (String title : titles) {
            Song song = new Song(null, title, artist, album, genre, year, false, 
                               timestamp, userId, albumArtUrl, albumArtStoragePath);
            songs.add(song);
        }
    }
} 