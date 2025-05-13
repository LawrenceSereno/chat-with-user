package com.example.adminuser;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class UserChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageView sendButton;
    private ImageView backButton;

    private FirebaseFirestore firestore;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String adminId = "admin";  // Placeholder admin ID
    private String userId;             // Passed from previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "Error: userId not provided!", Toast.LENGTH_LONG).show();
            Log.e("UserChatActivity", "userId was null. Did you forget to pass it in the intent?");
            finish();  // Exit to prevent further crashes
            return;
        }

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize the message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);

        // Set RecyclerView layout manager and adapter
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        // Fetch messages
        fetchMessages();

        // Send message button action
        sendButton.setOnClickListener(v -> sendMessage());

        // Back button action
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void fetchMessages() {
        String chatId = generateChatId(adminId, userId);

        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(UserChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        messageList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Message message = doc.toObject(Message.class);
                            messageList.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();
        Message message = new Message(adminId, userId, messageText, timestamp);

        String chatId = generateChatId(adminId, userId);

        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> messageEditText.setText(""))
                .addOnFailureListener(e -> Toast.makeText(UserChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show());
    }

    private String generateChatId(String id1, String id2) {
        if (id1 == null || id2 == null) return "invalid_chat_id";
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }
}
