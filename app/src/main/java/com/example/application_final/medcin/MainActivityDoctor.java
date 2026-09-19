package com.example.application_final.medcin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.io.File;

public class MainActivityDoctor extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        TextView welcomeText = findViewById(R.id.welcome_text);
        ImageView profileImage = findViewById(R.id.profile_image);

        if (userEmail != null) {
            String name = db.getUserName(userEmail);
            if (!name.isEmpty()) {
                welcomeText.setText("Bonjour, " + name);
            }
            
            // Charger la photo de profil dans le dashboard
            loadProfilePhoto(profileImage, db, userEmail);
        }

        // Listener sur l'icône I (notification_icon) pour aller vers le profil
        findViewById(R.id.notification_icon).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityDoctor.this, ProfileActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        findViewById(R.id.card_patients).setOnClickListener(
                v -> {
                    Intent intent = new Intent(this, com.example.application_final.secretaire.dossier_patient.class);
                    intent.putExtra("CAN_ADD", false);
                    startActivity(intent);
                });
        findViewById(R.id.card_planning).setOnClickListener(v -> {
            Intent intent = new Intent(this, planning.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
        findViewById(R.id.card_messages).setOnClickListener(v -> {
            Intent intent = new Intent(this, MessagesActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
        findViewById(R.id.card_labo).setOnClickListener(v -> {
            Intent intent = new Intent(this, LabResultsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        findViewById(R.id.card_prescriptions).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMedicineActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
    }

    private void loadProfilePhoto(ImageView profileImage, DatabaseHelper db, String userEmail) {
        String photoUri = db.getUserPhoto(userEmail);
        if (photoUri != null && !photoUri.isEmpty()) {
            try {
                File imgFile = new File(photoUri);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    if (bitmap != null) {
                        profileImage.setImageBitmap(bitmap);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Si le fichier n'existe pas, essayer comme URI
            try {
                Uri uri = Uri.parse(photoUri);
                profileImage.setImageURI(uri);
            } catch (Exception e) {
                e.printStackTrace();
                // Si tout échoue, garder l'image par défaut
            }
        }
    }
}
