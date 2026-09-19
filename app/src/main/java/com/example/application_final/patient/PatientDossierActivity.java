package com.example.application_final.patient;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;

public class PatientDossierActivity extends AppCompatActivity {

    DatabaseHelper db;
    String userEmail;
    TextView tvName, tvAntecedents, tvDescription, tvNoRecord;
    View contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dossier);

        db = DatabaseHelper.getInstance(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        tvName = findViewById(R.id.tvPatientName);
        tvAntecedents = findViewById(R.id.tvAntecedents);
        tvDescription = findViewById(R.id.tvDescription);
        tvNoRecord = findViewById(R.id.tvNoRecord);
        // contentLayout = findViewById(R.id.contentLayout); // If I had a wrapper, but I can just toggle visibility of individual cards or just text

        loadDossier();
    }

    private void loadDossier() {
        if (userEmail == null) return;

        int userId = db.getUserIdByEmail(userEmail);
        if (userId == -1) {
            showNoRecord();
            return;
        }

        Cursor c = db.getPatientByUserId(userId);
        if (c != null && c.moveToFirst()) {
            // Record found
            tvNoRecord.setVisibility(View.GONE);
            
            String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_FULLNAME));
            String antecedents = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_ANTECEDENTS));
            String description = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_DESCRIPTION));

            tvName.setText("Nom: " + name);
            tvAntecedents.setText(antecedents.isEmpty() ? "Aucun antécédent signalé." : antecedents);
            tvDescription.setText(description.isEmpty() ? "Aucune description disponible." : description);
            
            c.close();
        } else {
            showNoRecord();
        }
    }

    private void showNoRecord() {
        tvNoRecord.setVisibility(View.VISIBLE);
        tvName.setText("Information non disponible");
        tvAntecedents.setText("-");
        tvDescription.setText("-");
    }
}
