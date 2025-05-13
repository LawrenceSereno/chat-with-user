package com.example.adminuser;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.adminuser.Adapter.MessageAdapter;
import com.google.firebase.firestore.*;
import java.util.*;

public class AdminChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageView sendButton;

    private FirebaseFirestore firestore;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String adminId = "admin";
    private String userId;
    private String chatId;

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

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);

        userId = getIntent().getStringExtra("USER_ID");
        chatId = adminId + "_" + userId;

        listenToMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void listenToMessages() {
        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        messageList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
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

        // Send message to subcollection
        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(aVoid -> {
                    messageEditText.setText("");
                    updateChatMetadata(message);
                });
    }

    private void updateChatMetadata(Message message) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("users", Arrays.asList(adminId, userId));
        metadata.put("lastMessage", message.getMessage());
        metadata.put("lastTimestamp", message.getTimestamp());

        firestore.collection("chats")
                .document(chatId)
                .set(metadata, SetOptions.merge());
    }
}
