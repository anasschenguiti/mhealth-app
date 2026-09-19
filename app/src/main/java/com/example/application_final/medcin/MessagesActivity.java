package com.example.application_final.medcin;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ContactAdapter adapter;
    DatabaseHelper db;
    List<Contact> contactList = new ArrayList<>();
    String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        db = DatabaseHelper.getInstance(this);
        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
        recyclerView = findViewById(R.id.recycler_messages);

        adapter = new ContactAdapter(contactList, contact -> {
            Intent intent = new Intent(MessagesActivity.this, ChatActivity.class);
            intent.putExtra("CURRENT_USER", currentUserEmail); // Email
            intent.putExtra("OTHER_USER_EMAIL", contact.email); // Email for ID
            intent.putExtra("OTHER_USER_NAME", contact.name); // Name for Display
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadContacts();
    }

    private void loadContacts() {
        contactList.clear();
        if (currentUserEmail != null) {
            String role = db.getUserRole(currentUserEmail);
            Cursor c = db.getPotentialContacts(role);
            if (c != null) {
                while (c.moveToNext()) {
                    String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                    String email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL));
                    String contactRole = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE));
                    String specialty = "";
                    int specIndex = c.getColumnIndex(DatabaseHelper.COL_USER_SPECIALTY);
                    if (specIndex != -1) {
                        specialty = c.getString(specIndex);
                    }
                    if (specialty == null || specialty.isEmpty()) {
                        specialty = contactRole;
                    }
                    contactList.add(new Contact(name, email, specialty));
                }
                c.close();
            }
        }
        adapter.notifyDataSetChanged();
    }

    // --- Inner Classes ---

    static class Contact {
        String name;
        String email;
        String details; // Role or Specialty

        public Contact(String name, String email, String details) {
            this.name = name;
            this.email = email;
            this.details = details;
        }
    }

    interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
        List<Contact> list;
        OnContactClickListener listener;

        public ContactAdapter(List<Contact> list, OnContactClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Reusing item_medecin for look and feel (Name + Subtext)
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medecin, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Contact c = list.get(position);
            holder.name.setText(c.name);
            holder.details.setText(c.details);
            holder.itemView.setOnClickListener(v -> listener.onContactClick(c));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, details;

            public ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.docName);
                details = v.findViewById(R.id.docSpec);
            }
        }
    }
}
