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

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private Button btnLogin;
    private TextView textForgotPassword;
    private TextView textSignUp;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Check if already authenticated
        if (authViewModel.isUserAuthenticated()) {
            navigateToMain();
            return;
        }

        // Initialize views
        initializeViews();
        setupObservers();
        setupClickListeners();
    }

    private void initializeViews() {
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textForgotPassword = findViewById(R.id.textForgotPassword);
        textSignUp = findViewById(R.id.textSignUp);
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
            btnLogin.setEnabled(!isLoading);
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(btnLogin, error, Snackbar.LENGTH_LONG).show();
                authViewModel.clearErrorMessage();
            }
        });

        // Observe success messages
        authViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(btnLogin, message, Snackbar.LENGTH_SHORT).show();
                authViewModel.clearSuccessMessage();
            }
        });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        textSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });

        textForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleLogin() {
        String email = Objects.requireNonNull(editEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(editPassword.getText()).toString().trim();

        authViewModel.signIn(email, password);
    }

    private void handleForgotPassword() {
        String email = Objects.requireNonNull(editEmail.getText()).toString().trim();

        if (email.isEmpty()) {
            Snackbar.make(btnLogin, "Please enter your email first", Snackbar.LENGTH_SHORT).show();
            return;
        }

        authViewModel.resetPassword(email);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
