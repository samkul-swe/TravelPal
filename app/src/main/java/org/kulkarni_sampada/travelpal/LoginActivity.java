package org.kulkarni_sampada.travelpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import org.kulkarni_sampada.travelpal.firebase.AuthConnector;
import org.kulkarni_sampada.travelpal.firebase.repository.database.UserRepository;
import org.kulkarni_sampada.travelpal.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private String uid;
    private String name;

    private void addUserToDatabase() {
        // Creating a user
        User currentUser = new User();
        currentUser.setName(name);
        currentUser.setUserID(uid);

        // Get a reference to the user's data in the database
        UserRepository userRepository = new UserRepository(uid);
        DatabaseReference userRef = userRepository.getUserRef();

        // Save user in the database
        userRef.setValue(currentUser);
    }

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

                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                uid = user.getUid();
                name = user.getDisplayName();

                SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(AppConstants.UID_KEY, uid);
                editor.putString(AppConstants.USER_NAME, name);
                editor.apply();

                addUserToDatabase();

                Intent intent = new Intent(LoginActivity.this,  InterestsActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthFailure(Exception e) {
                // Authentication or user creation failed
                // Handle the error here
                Log.e("LoginActivity", "Authentication failed: " + e.getMessage());
            }
        });
    }
}