package com.example.adminuser.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.adminuser.Message;
import com.example.adminuser.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;

    // Constructor for initializing the list of messages
    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the message item layout (item_message.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Bind data to the views
        holder.senderTextView.setText(message.getSenderId());
        holder.messageTextView.setText(message.getMessage());

        // Format timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTimestamp = dateFormat.format(new Date(message.getTimestamp()));
        holder.timestampTextView.setText(formattedTimestamp);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder class that holds references to the views in each item
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView, messageTextView, timestampTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            // Initialize the TextViews
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
           // Ensure this exists in your layout
        }
    }
}
