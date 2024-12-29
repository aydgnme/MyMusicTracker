package me.aydgn.mymusictracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.aydgn.mymusictracker.model.User;

public class ProfileActivity extends AppCompatActivity {
    private ShapeableImageView profileImage;
    private TextInputEditText editFullName, editUsername, editEmail, editFavoriteGenres;
    private MaterialButton btnChangePhoto, btnSaveProfile, btnLogout;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(currentUser.getUid());

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Bind views
        profileImage = findViewById(R.id.profileImage);
        editFullName = findViewById(R.id.editFullName);
        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editFavoriteGenres = findViewById(R.id.editFavoriteGenres);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    profileImage.setImageURI(uri);
                }
            }
        );

        // Load user data
        loadUserData();

        // Setup click listeners
        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        editFullName.setText(user.getFullName());
                        editUsername.setText(user.getUsername());
                        editEmail.setText(user.getEmail());
                        
                        // Display favorite genres as comma-separated text
                        List<String> genres = user.getFavoriteGenres();
                        if (genres != null && !genres.isEmpty()) {
                            editFavoriteGenres.setText(TextUtils.join(", ", genres));
                        }

                        String profileImageUrl = user.getProfileImageUrl();
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Picasso.get().load(profileImageUrl).into(profileImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        final String fullName = editFullName.getText().toString().trim();
        final String username = editUsername.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String genresText = editFavoriteGenres.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert comma-separated genres to list
        final List<String> genres = new ArrayList<>();
        if (!genresText.isEmpty()) {
            genres.addAll(Arrays.asList(genresText.split("\\s*,\\s*")));
        }

        // Upload profile image
        if (selectedImageUri != null) {
            StorageReference imageRef = storageRef.child("profile.jpg");
            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> saveUserData(fullName, username, email, genres, uri.toString())))
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, getString(R.string.error_update_profile, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
        } else {
            saveUserData(fullName, username, email, genres, null);
        }
    }

    private void saveUserData(String fullName, String username, String email, List<String> genres, String profileImageUrl) {
        User user = new User(fullName, username, email, genres);
        if (profileImageUrl != null) {
            user.setProfileImageUrl(profileImageUrl);
        }

        userRef.setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, 
                    getString(R.string.error_update_profile, task.getException().getMessage()),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.confirm_logout)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                auth.signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            })
            .setNegativeButton(R.string.no, null)
            .show();
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