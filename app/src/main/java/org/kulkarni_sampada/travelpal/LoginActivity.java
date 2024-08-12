package org.kulkarni_sampada.travelpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import org.kulkarni_sampada.travelpal.firebase.AuthConnector;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        Button loginButton = findViewById(R.id.login_button);


        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        // Get the email and password from the EditText fields
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        AuthConnector.getFirebaseAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = AuthConnector.getFirebaseAuth().getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                            SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(AppConstants.UID_KEY, uid);
                            editor.putString(AppConstants.USER_NAME, user.getDisplayName());
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, TravelParametersActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
