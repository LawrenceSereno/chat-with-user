package com.example.adminuser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterUserActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private Button registerButton, goToLoginButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        goToLoginButton = findViewById(R.id.goToLoginButton);

        // Register Button logic
        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(RegisterUserActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Register the user
                registerUser(email, password, username);
            }
        });

        // Go to Login Button logic
        goToLoginButton.setOnClickListener(v -> {
            // Navigate to the Login Activity
            startActivity(new Intent(RegisterUserActivity.this, Login.class));
            finish(); // Optional: Close the current activity (register) so the user cannot return by pressing back
        });
    }

    private void registerUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registration successful, now save user data in Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data in Firestore
                            saveUserData(user.getUid(), username);
                        }
                    } else {
                        // If registration fails, display a message to the user
                        Toast.makeText(RegisterUserActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String userId, String username) {
        // Create a map to store user data (including role as 'admin')
        User user = new User(username, "admin");  // Set role as 'admin' for now
        firestore.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterUserActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    // Redirect to login screen or admin dashboard
                    startActivity(new Intent(RegisterUserActivity.this, AdminDashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterUserActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Create a User class to hold the user data (username and role)
    public static class User {
        private String username;
        private String role;

        // Constructor
        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }

        // Getters for username and role
        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
