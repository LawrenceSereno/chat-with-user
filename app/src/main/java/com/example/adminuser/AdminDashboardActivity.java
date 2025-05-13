package com.example.adminuser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.adminuser.Adapter.UserAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private FirebaseFirestore firestore;
    private List<User> usersList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        firestore = FirebaseFirestore.getInstance();
        usersList = new ArrayList<>();

        // Set up RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with a listener for user clicks
        userAdapter = new UserAdapter(this, usersList, user -> {
            // Handle user click here, for example, open a chat screen
            String userId = user.getUserId(); // You can pass this to the chat activity
            String username = user.getUsername(); // Get username from the clicked user
            Intent intent = new Intent(AdminDashboardActivity.this, AdminChatActivity.class);
            intent.putExtra("USER_ID", userId);  // Pass userId to AdminChatActivity
            intent.putExtra("USER_NAME", username);  // Pass username to AdminChatActivity
            startActivity(intent);  // Open chat activity
        });

        // Set the adapter to the RecyclerView
        usersRecyclerView.setAdapter(userAdapter);

        // Fetch users from Firestore
        fetchUsers();
    }

    private void fetchUsers() {
        firestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    usersList.clear();  // Clear any existing data in the list
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getString("userId");
                        String username = doc.getString("username");

                        // Add the user to the list
                        usersList.add(new User(userId, username));  // Create a new User object and add it
                    }

                    // Notify the adapter that the data has been updated
                    userAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                });
    }
}
