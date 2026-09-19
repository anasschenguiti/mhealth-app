package com.example.application_final.medcin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editMessage;
    private View btnSend, btnBack;
    private TextView titleChat;

    private ChatAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    private String currentUserEmail;
    private String otherUserEmail;
    private String otherUserNameDisplay;
    private MessageManager messageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        messageManager = MessageManager.getInstance(this);

        currentUserEmail = getIntent().getStringExtra("CURRENT_USER");
        otherUserEmail = getIntent().getStringExtra("OTHER_USER_EMAIL");
        otherUserNameDisplay = getIntent().getStringExtra("OTHER_USER_NAME");

        // Fallback for older intents if any
        if (otherUserEmail == null)
            otherUserEmail = getIntent().getStringExtra("OTHER_USER");
        if (otherUserNameDisplay == null)
            otherUserNameDisplay = otherUserEmail;

        if (currentUserEmail == null || otherUserEmail == null) {
            Toast.makeText(this, "Erreur de conversation", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadMessages();

        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_chat_messages);
        editMessage = findViewById(R.id.edit_message);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);
        titleChat = findViewById(R.id.title_chat);

        titleChat.setText("Conv. avec " + otherUserNameDisplay);
    }

    private void setupRecyclerView() {
        // We can reuse MessageAdapter if suitable, or create ChatAdapter.
        // Let's create a simple proper ChatAdapter in this same package if needed or
        // inner class.
        // Since MessageAdapter exists, let's check if it suits 'Chat' (left/right
        // bubbles).
        // The existing MessageAdapter (I saw file list but not content) probably lists
        // single rows.
        // I will create a dedicated ChatAdapter here or use the existing one if
        // compatible.
        // Given I haven't seen MessageAdapter content, I'll create an inner class
        // Adapter for safety and speed.

        adapter = new ChatAdapter(messageList, currentUserEmail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        messageManager.getMessages(currentUserEmail, otherUserEmail, new MessageManager.DataCallback<List<Message>>() {
            @Override
            public void onDataLoaded(List<Message> data) {
                messageList.clear();
                messageList.addAll(data);
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, "Erreur chargement: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = editMessage.getText().toString().trim();
        if (content.isEmpty())
            return;

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        editMessage.setText("");

        messageManager.sendMessage(currentUserEmail, otherUserEmail, content, time,
                new MessageManager.MessageCallback() {
                    @Override
                    public void onSuccess() {
                        loadMessages();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ChatActivity.this, "Echec envoi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
