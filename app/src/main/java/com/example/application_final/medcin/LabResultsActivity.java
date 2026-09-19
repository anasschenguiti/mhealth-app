package com.example.application_final.medcin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LabResultsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LabResultAdapter adapter;
    DatabaseHelper db;
    List<LabResult> list = new ArrayList<>();
    FloatingActionButton fabAdd;
    ImageClassifier imageClassifier;
    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_results);

        db = DatabaseHelper.getInstance(this);
        recyclerView = findViewById(R.id.recycler_lab_results);
        fabAdd = findViewById(R.id.fab_add_result);

        // Initialiser le classificateur d'images avec vérification
        imageClassifier = new ImageClassifier(this);
        if (!imageClassifier.isInitialized()) {
            String error = imageClassifier.getInitializationError();
            Log.e("LabResultsActivity", "Erreur d'initialisation: " + error);
            // Ne pas afficher de Toast ici pour éviter de spammer l'utilisateur
            // L'erreur sera affichée uniquement si l'utilisateur essaie d'utiliser la fonctionnalité
        }

        // Initialiser le launcher pour sélectionner une image
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            classifyImage(selectedImageUri);
                        }
                    }
                }
            }
        );

        adapter = new LabResultAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadResults();

        fabAdd.setOnClickListener(v -> showAddResultDialog());
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadResults() {
        list.clear();
        Cursor c = db.getLabResults();
        if (c != null) {
            while (c.moveToNext()) {
                String p = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAB_PATIENT));
                String t = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAB_TEST));
                String r = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAB_RESULT));
                String d = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAB_DATE));
                list.add(new LabResult(p, t, r, d));
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddResultDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter un Résultat");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputPatient = new EditText(this);
        inputPatient.setHint("Nom du Patient");
        layout.addView(inputPatient);

        final EditText inputTest = new EditText(this);
        inputTest.setHint("Type de Test / Examen");
        layout.addView(inputTest);

        final EditText inputResult = new EditText(this);
        inputResult.setHint("Résultat / Conclusion");
        layout.addView(inputResult);
        
        final EditText inputDate = new EditText(this);
        inputDate.setHint("Date (JJ/MM/AAAA)");
        inputDate.setFocusable(false);
        inputDate.setOnClickListener(v -> {
             Calendar cal = Calendar.getInstance();
             new DatePickerDialog(this, (dp, y, m, d) -> inputDate.setText(d + "/" + (m+1) + "/" + y),
                     cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        layout.addView(inputDate);

        // Bouton pour analyser une radiographie
        android.widget.Button btnAnalyzeXray = new android.widget.Button(this);
        btnAnalyzeXray.setText("Analyser une Radiographie");
        btnAnalyzeXray.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        btnAnalyzeXray.setPadding(20, 20, 20, 20);
        btnAnalyzeXray.setOnClickListener(v -> {
            // Ouvrir le sélecteur d'images
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
        layout.addView(btnAnalyzeXray);

        builder.setView(layout);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String p = inputPatient.getText().toString();
            String t = inputTest.getText().toString();
            String r = inputResult.getText().toString();
            String d = inputDate.getText().toString();

            if (p.isEmpty() || t.isEmpty() || r.isEmpty() || d.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            db.addLabResult(p, t, r, d);
            Toast.makeText(this, "Résultat ajouté", Toast.LENGTH_SHORT).show();
            loadResults();
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    /**
     * Classifie une image de radiographie sélectionnée
     */
    private void classifyImage(Uri imageUri) {
        // Vérifier que le classificateur est initialisé
        if (imageClassifier == null || !imageClassifier.isInitialized()) {
            String errorMsg = imageClassifier != null ? imageClassifier.getInitializationError() : "Le classificateur n'est pas initialisé";
            
            AlertDialog.Builder errorBuilder = new AlertDialog.Builder(this);
            errorBuilder.setTitle("Erreur d'initialisation");
            errorBuilder.setMessage("Impossible d'analyser l'image. " + errorMsg + 
                "\n\nVérifiez que:\n" +
                "1. Le dossier 'assets' existe dans app/src/main/\n" +
                "2. Le fichier model.tflite existe dans assets/converted_tflite_quantized/\n" +
                "3. Le fichier labels.txt existe dans assets/converted_tflite_quantized/");
            errorBuilder.setPositiveButton("OK", null);
            errorBuilder.show();
            return;
        }

        try {
            // Charger l'image depuis l'URI
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            
            if (bitmap == null) {
                Toast.makeText(this, "Impossible de charger l'image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Afficher un dialogue de chargement
            AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Analyse de la radiographie en cours...")
                .setCancelable(false)
                .create();
            loadingDialog.show();

            // Effectuer la classification dans un thread séparé pour éviter de bloquer l'UI
            new Thread(() -> {
                try {
                    ImageClassifier.ClassificationResult result = imageClassifier.classifyImage(bitmap);
                    
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        showClassificationResult(result);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(LabResultsActivity.this, 
                            "Erreur lors de la classification: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    });
                }
            }).start();

        } catch (IOException e) {
            Toast.makeText(this, "Erreur lors du chargement de l'image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur inattendue: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Affiche le résultat de la classification dans un dialogue
     */
    private void showClassificationResult(ImageClassifier.ClassificationResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Résultat de l'Analyse de Radiographie");

        // Créer une vue personnalisée pour afficher les résultats
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        TextView labelText = new TextView(this);
        labelText.setText("Diagnostic: " + result.getLabel());
        labelText.setTextSize(18);
        labelText.setTextColor(getResources().getColor(android.R.color.black, null));
        labelText.setPadding(0, 0, 0, 16);
        layout.addView(labelText);

        TextView urgencyText = new TextView(this);
        urgencyText.setText("Niveau d'urgence: " + result.getUrgencyLevel());
        urgencyText.setTextSize(16);
        
        // Colorer selon le niveau d'urgence
        if (result.getUrgencyLevel().contains("URGENT")) {
            urgencyText.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        } else if (result.getUrgencyLevel().contains("MOYEN")) {
            urgencyText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));
        } else {
            urgencyText.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        }
        
        urgencyText.setPadding(0, 0, 0, 16);
        layout.addView(urgencyText);

        TextView confidenceText = new TextView(this);
        confidenceText.setText("Confiance: " + result.getConfidencePercentage());
        confidenceText.setTextSize(14);
        confidenceText.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        layout.addView(confidenceText);

        builder.setView(layout);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageClassifier != null) {
            imageClassifier.close();
        }
    }
}
