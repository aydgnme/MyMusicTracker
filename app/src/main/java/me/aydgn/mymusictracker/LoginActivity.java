package me.aydgn.mymusictracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import androidx.annotation.Nullable;
import android.widget.CheckBox;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "login_pref";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LOGIN_TYPE = "login_type";
    private static final String LOGIN_TYPE_EMAIL = "email";
    private static final String LOGIN_TYPE_GOOGLE = "google";
    private static final String KEY_GOOGLE_TOKEN = "google_token";
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private ProgressDialog progressDialog;
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        initializeViews();
        checkRememberedUser();
        setupClickListeners();

        // Initialize Google Sign In Launcher
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(LoginActivity.this, 
                            getString(R.string.error_login_failed, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheckBox = findViewById(R.id.checkboxRememberMe);
    }

    private void checkRememberedUser() {
        if (sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            String loginType = sharedPreferences.getString(KEY_LOGIN_TYPE, "");
            if (LOGIN_TYPE_EMAIL.equals(loginType)) {
                String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
                String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
                emailInput.setText(savedEmail);
                passwordInput.setText(savedPassword);
                rememberMeCheckBox.setChecked(true);
                // Automatic login with email
                signInWithEmail();
            } else if (LOGIN_TYPE_GOOGLE.equals(loginType)) {
                String savedToken = sharedPreferences.getString(KEY_GOOGLE_TOKEN, "");
                if (!savedToken.isEmpty()) {
                    // Automatic login with Google
                    firebaseAuthWithGoogle(savedToken);
                }
            }
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.loginButton).setOnClickListener(v -> handleLogin());
        findViewById(R.id.googleSignInButton).setOnClickListener(v -> handleGoogleSignIn());
        findViewById(R.id.registerButton).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.getIdToken(true)
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String idToken = tokenTask.getResult().getToken();
                                            // Remember Me option processing
                                            if (rememberMeCheckBox.isChecked()) {
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean(KEY_REMEMBER_ME, true);
                                                editor.putString(KEY_EMAIL, email);
                                                editor.putString(KEY_GOOGLE_TOKEN, idToken);
                                                editor.apply();
                                            } else {
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.clear();
                                                editor.apply();
                                            }
                                            startMainActivity();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " +
                                task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Sign out current session and start new sign-in process
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            try {
                startActivityForResult(signInIntent, RC_SIGN_IN);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.error_message, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                String errorMessage;
                switch (e.getStatusCode()) {
                    case GoogleSignInStatusCodes.NETWORK_ERROR:
                        errorMessage = "Network connection error. Please check your internet connection.";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                        errorMessage = "Sign-in process cancelled.";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                        errorMessage = "Sign-in failed. Please try again.";
                        break;
                    default:
                        errorMessage = "Sign-in error: " + e.getMessage();
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save credentials for Remember Me
                            saveGoogleCredentials(idToken);
                            updateUI(user);
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Google sign-in failed";
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        updateUI(null);
                    }
                });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Signing in...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User successfully logged in, redirect to main screen
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            // Login failed, reset UI
            emailInput.setText("");
            passwordInput.setText("");
            rememberMeCheckBox.setChecked(false);
            // Clear saved information
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
    }

    private void loadSavedCredentials() {
        if (sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            String loginType = sharedPreferences.getString(KEY_LOGIN_TYPE, "");
            
            if (LOGIN_TYPE_EMAIL.equals(loginType)) {
                String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
                String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
                emailInput.setText(savedEmail);
                passwordInput.setText(savedPassword);
                rememberMeCheckBox.setChecked(true);
                
                // Automatic login with email
                signInWithEmail();
            } else if (LOGIN_TYPE_GOOGLE.equals(loginType)) {
                String savedToken = sharedPreferences.getString(KEY_GOOGLE_TOKEN, "");
                if (!savedToken.isEmpty()) {
                    // Automatic login with Google
                    firebaseAuthWithGoogle(savedToken);
                }
            }
        }
    }

    private void saveEmailCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (rememberMeCheckBox.isChecked()) {
            editor.putBoolean(KEY_REMEMBER_ME, true);
            editor.putString(KEY_LOGIN_TYPE, LOGIN_TYPE_EMAIL);
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_PASSWORD, password);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    private void saveGoogleCredentials(String idToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER_ME, true);
        editor.putString(KEY_LOGIN_TYPE, LOGIN_TYPE_GOOGLE);
        editor.putString(KEY_GOOGLE_TOKEN, idToken);
        editor.apply();
    }

    private void signInWithEmail() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveEmailCredentials(email, password);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.error_login_failed, task.getException().getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 