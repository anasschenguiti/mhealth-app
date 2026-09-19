package com.example.application_final.secretaire;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;

public class MainActivitySecretary extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secretary_main);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        TextView welcomeText = findViewById(R.id.welcomeText);

        if (userEmail != null) {
            String name = db.getUserName(userEmail);
            if (!name.isEmpty()) {
                welcomeText.setText("Bonjour, " + name);
            }
        }

        View btnPatients = findViewById(R.id.btnPatients);
        View btnRendezvous = findViewById(R.id.btnRendezvous);
        View btnPlanning = findViewById(R.id.btnPlanning);
        View btnMessages = findViewById(R.id.btnMessages);

        btnPatients.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivitySecretary.this, dossier_patient.class);
            intent.putExtra("CAN_ADD", true);
            startActivity(intent);
        });

        // Appointment handling linked to Secretaire rendezvous activity
        btnRendezvous.setOnClickListener(v -> startActivity(new Intent(MainActivitySecretary.this, rendezvous.class)));

        // Link Planning to medcin.planning (will update planning.java to handle cleaner
        // view for secretary)
        btnPlanning.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivitySecretary.this, com.example.application_final.medcin.planning.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        // Link Messages to medcin.MessagesActivity
        btnMessages.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivitySecretary.this,
                    com.example.application_final.medcin.MessagesActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivitySecretary.this, com.example.application_final.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
