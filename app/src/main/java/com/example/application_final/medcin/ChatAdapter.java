package com.example.application_final.medcin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserEmail;

    public ChatAdapter(List<Message> messages, String currentUserEmail) {
        this.messages = messages;
        this.currentUserEmail = currentUserEmail;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        if (msg.getSender().equals(currentUserEmail)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message_sent, parent, false);
            return new SentViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message_received, parent,
                    false);
            return new ReceivedViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(msg);
        } else {
            ((ReceivedViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView content, time;

        SentViewHolder(View v) {
            super(v);
            content = v.findViewById(R.id.message_text);
            time = v.findViewById(R.id.message_time);
        }

        void bind(Message m) {
            content.setText(m.getContent());
            time.setText(m.getTime());
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView content, time;

        ReceivedViewHolder(View v) {
            super(v);
            content = v.findViewById(R.id.message_text);
            time = v.findViewById(R.id.message_time);
        }

        void bind(Message m) {
            content.setText(m.getContent());
            time.setText(m.getTime());
        }
    }
}
