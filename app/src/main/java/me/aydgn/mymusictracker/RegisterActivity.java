package me.aydgn.mymusictracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registration activity that handles user sign up process
 * Collects user's personal information, password and music preferences
 */
public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference database;
    private TextInputEditText nameInput, usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private ChipGroup genreChipGroup;
    private List<String> selectedGenres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Initialize view bindings
        nameInput = findViewById(R.id.nameInput);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        genreChipGroup = findViewById(R.id.genreChipGroup);
        MaterialButton registerButton = findViewById(R.id.registerButton);
        MaterialButton loginButton = findViewById(R.id.loginButton);

        // Create chips for music genres
        String[] genres = getResources().getStringArray(R.array.music_genres);
        for (String genre : genres) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setChipBackgroundColorResource(R.color.spotify_dark_gray);
            chip.setChipStrokeColorResource(R.color.spotify_green);
            chip.setChipStrokeWidth(getResources().getDimension(R.dimen.chip_stroke_width));
            chip.setTextColor(getResources().getColor(R.color.spotify_white, getTheme()));
            chip.setCheckedIconTintResource(R.color.spotify_green);
            chip.setRippleColorResource(R.color.spotify_green);
            
            // Handle chip selection
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedGenres.add(genre);
                } else {
                    selectedGenres.remove(genre);
                }
                updateGenreInput();
            });
            
            genreChipGroup.addView(chip);
        }

        // Set click listeners
        registerButton.setOnClickListener(v -> registerUser());
        loginButton.setOnClickListener(v -> finish());
    }

    /**
     * Updates the genre input field with selected genres
     * Displays genres as comma-separated text
     */
    private void updateGenreInput() {
        TextInputEditText genreInput = findViewById(R.id.genreInput);
        if (selectedGenres.isEmpty()) {
            genreInput.setText("");
        } else {
            genreInput.setText(String.join(", ", selectedGenres));
        }
    }

    /**
     * Validates password strength
     * Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one number
     * - At least one special character (@#$%^&+=!)
     * - No whitespace allowed
     */
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }

    /**
     * Handles user registration process
     * Validates user input and creates account in Firebase
     */
    private void registerUser() {
        // Get user input
        String name = nameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate required fields
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty() || selectedGenres.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.error_passwords_not_match, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password strength
        if (!isValidPassword(password)) {
            Toast.makeText(this, R.string.error_password_requirements, Toast.LENGTH_LONG).show();
            return;
        }

        // Create user account in Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save user data to Realtime Database
                        String userId = auth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("username", username);
                        user.put("email", email);
                        user.put("favoriteGenres", selectedGenres);
                        user.put("createdAt", System.currentTimeMillis());

                        database.child("users").child(userId).setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, 
                                            R.string.success_registration,
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(RegisterActivity.this,
                                                getString(R.string.error_save_user_data, e.getMessage()),
                                                Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.error_registration_failed, task.getException().getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 