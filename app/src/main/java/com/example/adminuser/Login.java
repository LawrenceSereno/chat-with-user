package com.example.adminuser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private static final String TAG = "LoginActivity"; // For Logcat debugging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting Login Activity onCreate");
        setContentView(R.layout.activity_login);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        Log.d(TAG, "Views initialized successfully");

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Login button click
        loginButton.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");

            // Get input values
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            Log.d(TAG, "Email field value: " + (TextUtils.isEmpty(email) ? "EMPTY" : "PROVIDED"));
            Log.d(TAG, "Password field value: " + (TextUtils.isEmpty(password) ? "EMPTY" : "PROVIDED"));

            // Validate input
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        Log.d(TAG, "Starting loginUser method with email: " + email);

        // Show loading indicator
        Toast.makeText(Login.this, "Logging in...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication successful");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {
                            Log.e(TAG, "Authentication successful but user is null");
                            Toast.makeText(Login.this, "Login failed: user not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = user.getUid();
                        String userEmail = user.getEmail();
                        Log.d(TAG, "User ID: " + uid);
                        Log.d(TAG, "User Email: " + userEmail);

                        // Fetch user role from Firestore
                        firestore.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String role = documentSnapshot.getString("role");
                                        Log.d(TAG, "User role: " + role);

                                        if ("admin".equalsIgnoreCase(role)) {
                                            Log.d(TAG, "Starting AdminDashboardActivity");
                                            Intent intent = new Intent(Login.this, AdminDashboardActivity.class);
                                            intent.putExtra("userId", uid);
                                            startActivity(intent);
                                        } else {
                                            Log.d(TAG, "Starting UserChatActivity with userId: " + uid);
                                            Intent intent = new Intent(Login.this, UserChatActivity.class);
                                            intent.putExtra("userId", uid);  // Pass userId to UserChatActivity
                                            startActivity(intent);
                                        }
                                        finish();
                                    } else {
                                        Log.e(TAG, "User document does not exist in Firestore");
                                        Toast.makeText(Login.this, "User data does not exist", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Firestore error: ", e);
                                    Toast.makeText(Login.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Log.e(TAG, "Login error: ", task.getException());
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() :
                                "Authentication failed";
                        Toast.makeText(Login.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}