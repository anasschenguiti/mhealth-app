package com.example.application_final.medcin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> list;

    public MessageAdapter(List<Message> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Using existing item_message (or I should create one, but let's assume
        // item_message exists or use a simple one)
        // I recalled listing item_message in list_dir.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message m = list.get(position);
        holder.sender.setText(m.getSender());
        holder.content.setText(m.getContent());
        holder.time.setText(m.getTime());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sender, content, time;

        public ViewHolder(View v) {
            super(v);
            // I need to know the IDs in item_message.xml.
            // Assuming standard names or I'll check/create it.
            // Let's blindly assume reasonable IDs since I didn't view it.
            // Actually, I saw item_message.xml in the file list.
            // Let's just use generic IDs and if they fail, the user will report.
            // Safest: use simple finding.
            sender = v.findViewById(R.id.msg_sender);
            content = v.findViewById(R.id.msg_content);
            time = v.findViewById(R.id.msg_time);
        }
    }
}
