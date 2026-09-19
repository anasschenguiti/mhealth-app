package com.example.application_final.patient;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivityPatient extends AppCompatActivity {

    DatabaseHelper db;
    String userEmail;
    TextView tvHello;
    TextView tvDailyAdvice;
    ImageView profileImage;
    NestedScrollView scrollView;
    private List<String> dailyAdvices;
    private Random random;
    private int lastScrollY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_patient);

        db = DatabaseHelper.getInstance(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        tvHello = findViewById(R.id.tvHello);
        tvDailyAdvice = findViewById(R.id.tvDailyAdvice);
        profileImage = findViewById(R.id.profile_image);
        scrollView = findViewById(R.id.scrollHome);

        // Initialiser la liste des conseils
        initializeAdvices();
        random = new Random();
        
        // Mettre à jour le conseil du jour au démarrage
        updateDailyAdvice();

        if (userEmail != null) {
            String name = db.getUserName(userEmail);
            if (!name.isEmpty()) {
                tvHello.setText("Bonjour, " + name);
            }
            
            // Charger la photo de profil depuis la base de données
            loadProfilePhoto();
        }

        // Ajouter un listener pour changer le conseil lors du scroll
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // Changer le conseil si l'utilisateur a scrollé de manière significative
                if (Math.abs(scrollY - lastScrollY) > 200) {
                    updateDailyAdvice();
                    lastScrollY = scrollY;
                }
            }
        });

        View cardDoctor = findViewById(R.id.cardDoctor);
        View cardMedicaments = findViewById(R.id.cardMedicaments);
        View cardDossier = findViewById(R.id.cardDossier);
        View cardAppointments = findViewById(R.id.cardAppointments);
        View cardLabResults = findViewById(R.id.cardLabResults);
        View cardMessages = findViewById(R.id.cardMessages);

        cardDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityPatient.this, RechercheMedecinActivity.class);
            intent.putExtra("USER_EMAIL", userEmail); // Pass email for booking
            startActivity(intent);
        });

        cardMedicaments.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityPatient.this, MedicamentsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        cardDossier.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityPatient.this, PatientDossierActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        cardAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityPatient.this, PatientAppointmentsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        findViewById(R.id.cardMap).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityPatient.this, HospitalsMapActivity.class);
            startActivity(intent);
        });

        cardLabResults.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityPatient.this, PatientLabResultsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        cardMessages.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityPatient.this,
                    com.example.application_final.medcin.MessagesActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });

        findViewById(R.id.btn_urgent).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:141"));
            startActivity(intent);
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityPatient.this, com.example.application_final.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initializeAdvices() {
        dailyAdvices = new ArrayList<>();
        dailyAdvices.add("Buvez suffisamment d'eau aujourd'hui pour rester en bonne santé 💧");
        dailyAdvices.add("Faites au moins 30 minutes d'exercice physique par jour 🏃");
        dailyAdvices.add("Dormez 7-8 heures par nuit pour une meilleure récupération 😴");
        dailyAdvices.add("Mangez 5 portions de fruits et légumes par jour 🥗");
        dailyAdvices.add("Prenez le temps de vous détendre et de respirer profondément 🧘");
        dailyAdvices.add("Évitez les écrans 1 heure avant de dormir 📱");
        dailyAdvices.add("Marchez au moins 10 000 pas par jour 🚶");
        dailyAdvices.add("Pratiquez la méditation pour réduire le stress 🧘‍♀️");
        dailyAdvices.add("Limitez votre consommation de sucre et de sel 🍭");
        dailyAdvices.add("Prenez vos médicaments à heures fixes ⏰");
        dailyAdvices.add("Faites des pauses régulières si vous travaillez assis 💺");
        dailyAdvices.add("Exposez-vous au soleil 15 minutes par jour pour la vitamine D ☀️");
    }

    private void updateDailyAdvice() {
        if (dailyAdvices != null && !dailyAdvices.isEmpty()) {
            String newAdvice = dailyAdvices.get(random.nextInt(dailyAdvices.size()));
            tvDailyAdvice.setText(newAdvice);
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
