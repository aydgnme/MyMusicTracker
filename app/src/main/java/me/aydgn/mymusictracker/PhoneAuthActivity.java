package me.aydgn.mymusictracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private String verificationId;
    private TextInputLayout phoneLayout, codeLayout;
    private TextInputEditText phoneInput, codeInput;
    private MaterialButton sendCodeButton, verifyCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        auth = FirebaseAuth.getInstance();

        // View links
        phoneLayout = findViewById(R.id.phoneLayout);
        codeLayout = findViewById(R.id.codeLayout);
        phoneInput = findViewById(R.id.phoneInput);
        codeInput = findViewById(R.id.codeInput);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);

        // Click listeners
        sendCodeButton.setOnClickListener(v -> sendVerificationCode());
        verifyCodeButton.setOnClickListener(v -> verifyCode());
    }

    private void sendVerificationCode() {
        String phoneNumber = phoneInput.getText().toString().trim();
        
        if (phoneNumber.isEmpty()) {
            phoneLayout.setError(getString(R.string.error_phone_required));
            return;
        }

        // Add country code for Romania
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+40" + phoneNumber;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(PhoneAuthActivity.this,
                                    getString(R.string.error_invalid_phone_number),
                                    Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(PhoneAuthActivity.this,
                                    getString(R.string.error_too_many_requests),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PhoneAuthActivity.this,
                                    getString(R.string.msg_verification_failed, e.getMessage()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String vId,
                                         @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = vId;
                        Toast.makeText(PhoneAuthActivity.this,
                                R.string.msg_code_sent,
                                Toast.LENGTH_SHORT).show();
                        
                        // UI update
                        phoneLayout.setEnabled(false);
                        sendCodeButton.setEnabled(false);
                        codeLayout.setVisibility(View.VISIBLE);
                        verifyCodeButton.setVisibility(View.VISIBLE);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode() {
        String code = codeInput.getText().toString().trim();
        
        if (code.isEmpty()) {
            codeLayout.setError(getString(R.string.error_code_required));
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PhoneAuthActivity.this,
                                R.string.msg_verification_success,
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PhoneAuthActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(PhoneAuthActivity.this,
                                getString(R.string.msg_verification_failed, task.getException().getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 