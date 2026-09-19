package com.example.application_final.admin;

import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main_page);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        TextView welcomeText = findViewById(R.id.textView2);

        if (userEmail != null) {
            String name = db.getUserName(userEmail);
            if (!name.isEmpty()) {
                welcomeText.setText("Bonjour, " + name);
            }
        }

        View cardManagement = findViewById(R.id.card_admin1);
        View cardSupervision = findViewById(R.id.card_admin3);

        cardManagement.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MainActivity2.class)));
        cardSupervision
                .setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SupervisionActivity.class)));

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.application_final.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
