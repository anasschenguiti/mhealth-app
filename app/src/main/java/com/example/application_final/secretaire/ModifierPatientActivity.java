package com.example.application_final.secretaire;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;

public class ModifierPatientActivity extends AppCompatActivity {

    EditText nameInput, antecedentsInput, descriptionInput;
    Button updateButton;
    DatabaseHelper db;
    String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_patient);

        db = DatabaseHelper.getInstance(this);
        patientId = getIntent().getStringExtra("id");

        nameInput = findViewById(R.id.editPatientName2);
        // Age input removed
        antecedentsInput = findViewById(R.id.editPatientAntecedents2);
        descriptionInput = findViewById(R.id.editPatientDescription2);
        updateButton = findViewById(R.id.btnUpdatePatient);

        loadData();

        updateButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String antecedents = antecedentsInput.getText().toString();
            String desc = descriptionInput.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir le nom", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.updatePatient(patientId, name, antecedents, desc)) {
                Toast.makeText(this, "Patient mis à jour", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        Cursor c = db.getPatientById(patientId);
        if (c != null && c.moveToFirst()) {
            nameInput.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_FULLNAME)));
            // Age column removed from DB, so we don't fetch it
            antecedentsInput.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_ANTECEDENTS)));
            descriptionInput.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_DESCRIPTION)));
            c.close();
        }
    }
}
