package com.example.adminuser;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adminuser.Adapter.MessageAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageView sendButton;

    private FirebaseFirestore firestore;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String adminId = "admin";  // Admin ID
    private String userId = "user";    // User ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        firestore = FirebaseFirestore.getInstance();
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);

        // Set up RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        // Fetch messages from Firestore
        fetchMessages();

        // Send message when button is clicked
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void fetchMessages() {
        firestore.collection("chats")
                .whereArrayContains("users", userId) // Fetch messages for admin and user
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(AdminChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    messageList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String senderId = doc.getString("senderId");
                        String receiverId = doc.getString("receiverId");
                        String messageText = doc.getString("message");
                        long timestamp = doc.getLong("timestamp");

                        Message message = new Message(senderId, receiverId, messageText, timestamp);
                        messageList.add(message);
                    }
                    messageAdapter.notifyDataSetChanged();
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        // Create the message object
        Message message = new Message(adminId, userId, messageText, timestamp);

        // Send the message to Firestore
        firestore.collection("chats")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageEditText.setText("");  // Clear the input field
                    fetchMessages();              // Refresh the chat
                })
                .addOnFailureListener(e -> Toast.makeText(AdminChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show());
    }
}
