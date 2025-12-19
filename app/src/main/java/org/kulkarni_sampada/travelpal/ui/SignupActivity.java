package org.kulkarni_sampada.travelpal.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.kulkarni_sampada.travelpal.R;
import org.kulkarni_sampada.travelpal.viewmodel.AuthViewModel;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    private TextInputEditText editName;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private TextInputEditText editConfirmPassword;
    private Button btnSignup;
    private TextView textLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize views
        initializeViews();
        setupObservers();
        setupClickListeners();
    }

    private void initializeViews() {
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        textLogin = findViewById(R.id.textLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupObservers() {
        // Observe authentication state
        authViewModel.getIsAuthenticated().observe(this, isAuthenticated -> {
            if (isAuthenticated) {
                navigateToMain();
            }
        });

        // Observe loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? ProgressBar.VISIBLE : ProgressBar.GONE);
            btnSignup.setEnabled(!isLoading);
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(btnSignup, error, Snackbar.LENGTH_LONG).show();
                authViewModel.clearErrorMessage();
            }
        });

        // Observe success messages
        authViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(btnSignup, message, Snackbar.LENGTH_SHORT).show();
                authViewModel.clearSuccessMessage();
            }
        });
    }

    private void setupClickListeners() {
        btnSignup.setOnClickListener(v -> handleSignup());

        textLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void handleSignup() {
        String name = Objects.requireNonNull(editName.getText()).toString().trim();
        String email = Objects.requireNonNull(editEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(editConfirmPassword.getText()).toString().trim();

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            Snackbar.make(btnSignup, "Passwords do not match", Snackbar.LENGTH_SHORT).show();
            return;
        }

        authViewModel.signUp(email, password, name);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}