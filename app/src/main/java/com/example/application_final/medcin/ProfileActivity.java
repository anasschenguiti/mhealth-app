package com.example.application_final.medcin;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    DatabaseHelper db;
    String userEmail;
    ImageView profileImage;
    ImageView btnBack;
    TextView textName, textEmail, textPhone, textSpecialty, textRpps, textAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = DatabaseHelper.getInstance(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        profileImage = findViewById(R.id.profile_image);
        btnBack = findViewById(R.id.btn_back);
        textName = findViewById(R.id.text_name);
        textEmail = findViewById(R.id.text_email);
        textPhone = findViewById(R.id.text_phone);
        textSpecialty = findViewById(R.id.text_specialty);
        textRpps = findViewById(R.id.text_rpps);
        textAddress = findViewById(R.id.text_address);

        // Charger les données du profil
        if (userEmail != null) {
            loadProfileData();
            loadProfilePhoto();
        }

        // Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // Bouton de déconnexion
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, com.example.application_final.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfileData() {
        int userId = db.getUserIdByEmail(userEmail);
        if (userId != -1) {
            Cursor c = db.getUserById(String.valueOf(userId));
            if (c != null && c.moveToFirst()) {
                // Nom complet
                int nameIndex = c.getColumnIndex(DatabaseHelper.COL_USER_NAME);
                if (nameIndex != -1) {
                    String name = c.getString(nameIndex);
                    textName.setText(name != null ? name : "Non renseigné");
                }

                // Email
                int emailIndex = c.getColumnIndex(DatabaseHelper.COL_USER_EMAIL);
                if (emailIndex != -1) {
                    String email = c.getString(emailIndex);
                    textEmail.setText(email != null ? email : "Non renseigné");
                }

                // Téléphone
                int phoneIndex = c.getColumnIndex(DatabaseHelper.COL_USER_PHONE);
                if (phoneIndex != -1) {
                    String phone = c.getString(phoneIndex);
                    textPhone.setText(phone != null && !phone.isEmpty() ? phone : "Non renseigné");
                }

                // Spécialité
                int specialtyIndex = c.getColumnIndex(DatabaseHelper.COL_USER_SPECIALTY);
                if (specialtyIndex != -1) {
                    String specialty = c.getString(specialtyIndex);
                    textSpecialty.setText(specialty != null && !specialty.isEmpty() ? specialty : "Non renseigné");
                }

                // RPPS (non stocké dans la DB, donc on laisse vide ou "Non renseigné")
                textRpps.setText("Non renseigné");

                // Adresse
                int addressIndex = c.getColumnIndex(DatabaseHelper.COL_USER_ADDRESS);
                if (addressIndex != -1) {
                    String address = c.getString(addressIndex);
                    textAddress.setText(address != null && !address.isEmpty() ? address : "Non renseigné");
                }

                c.close();
            }
        }
    }

    private void loadProfilePhoto() {
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

