package org.kulkarni_sampada.travelpal;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;

import org.kulkarni_sampada.travelpal.firebase.AuthConnector;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Load the XML layout
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);

        String email = String.valueOf(emailEditText.getText());
        String password = String.valueOf(passwordEditText.getText());

        AuthConnector.signInOrCreateUser(email, password, new AuthConnector.OnAuthResultListener() {
            @Override
            public void onAuthSuccess(FirebaseUser user) {
                // User is signed in or created successfully
                // You can now perform additional actions with the user object
                
            }

            @Override
            public void onAuthFailure(Exception e) {
                // Authentication or user creation failed
                // Handle the error here
            }
        });
    }
}