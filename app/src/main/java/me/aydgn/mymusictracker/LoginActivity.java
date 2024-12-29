package me.aydgn.mymusictracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialCheckBox rememberMeCheckbox;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheckbox = findViewById(R.id.checkboxRememberMe);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton googleSignInButton = findViewById(R.id.googleSignInButton);
        MaterialButton phoneSignInButton = findViewById(R.id.phoneSignInButton);

        // Kayıtlı bilgileri yükle
        if (sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            emailInput.setText(sharedPreferences.getString(KEY_EMAIL, ""));
            passwordInput.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
            rememberMeCheckbox.setChecked(true);
            // Otomatik giriş yap
            loginWithSavedCredentials();
        }

        loginButton.setOnClickListener(v -> handleLogin());
        googleSignInButton.setOnClickListener(v -> handleGoogleSignIn());
        phoneSignInButton.setOnClickListener(v -> handlePhoneSignIn());
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Beni hatırla seçeneği işaretliyse bilgileri kaydet
                        if (rememberMeCheckbox.isChecked()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(KEY_REMEMBER_ME, true);
                            editor.putString(KEY_EMAIL, email);
                            editor.putString(KEY_PASSWORD, password);
                            editor.apply();
                        } else {
                            // Beni hatırla seçili değilse kayıtlı bilgileri temizle
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                        }
                        // Ana ekrana yönlendir
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, 
                            getString(R.string.error_login_failed, task.getException().getMessage()),
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginWithSavedCredentials() {
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String password = sharedPreferences.getString(KEY_PASSWORD, "");

        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Otomatik giriş başarısız olursa kayıtlı bilgileri temizle
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                        }
                    });
        }
    }

    private void handleGoogleSignIn() {
        // Google ile giriş işlemleri
    }

    private void handlePhoneSignIn() {
        // Telefon ile giriş işlemleri
    }
} 