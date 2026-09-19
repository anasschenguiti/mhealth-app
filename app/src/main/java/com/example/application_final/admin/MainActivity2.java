package com.example.application_final.admin;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;

public class MainActivity2 extends AppCompatActivity {

    DatabaseHelper db;
    LinearLayout usersContainer; // Inside ScrollView
    EditText searchInput;
    View btnAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gestion_des_utilisateurs);

        db = DatabaseHelper.getInstance(this);
        usersContainer = findViewById(R.id.users_container);
        searchInput = findViewById(R.id.search_input);
        btnAddUser = findViewById(R.id.btn_add_user);

        btnAddUser.setOnClickListener(v -> startActivity(new Intent(MainActivity2.this, MainActivity3.class)));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadUsers(s.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers("");
    }

    private void loadUsers(String query) {
        // Clear existing views except the Search Input (index 0)
        // Wait, search input is IN the container? Let's check layout.
        // Layout: users_container contains TextInputLayout (index 0).
        // So we remove views from index 1 onwards.
        int childCount = usersContainer.getChildCount();
        if (childCount > 1) {
            usersContainer.removeViews(1, childCount - 1);
        }

        Cursor c = query.isEmpty() ? db.getAllUsers() : db.searchUsers(query);
        if (c != null) {
            while (c.moveToNext()) {
                String id = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                String role = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE));

                View itemView = LayoutInflater.from(this).inflate(R.layout.item_user, usersContainer, false);

                TextView tvName = itemView.findViewById(R.id.user_name);
                ImageView btnEdit = itemView.findViewById(R.id.edit_user);
                ImageView btnDelete = itemView.findViewById(R.id.delete_user);

                tvName.setText(name + " (" + role + ")");

                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                    intent.putExtra("USER_ID", id);
                    startActivity(intent);
                });

                btnDelete.setOnClickListener(v -> {
                    db.deleteUser(id);
                    loadUsers(searchInput.getText().toString());
                    Toast.makeText(this, "Utilisateur supprimé", Toast.LENGTH_SHORT).show();
                });

                usersContainer.addView(itemView);
            }
            c.close();
        }
    }
}
