package com.example.application_final.medcin;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class AddMedicineActivity extends AppCompatActivity {

    Spinner spinnerPatient;
    TextInputEditText etMedicineName, etDosage, etFrequency, etDuration;
    Button btnSave;
    DatabaseHelper db;
    List<String> patientNames = new ArrayList<>();
    List<String> patientEmails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        db = DatabaseHelper.getInstance(this);

        spinnerPatient = findViewById(R.id.spinner_patient);
        etMedicineName = findViewById(R.id.input_medicine_name);
        etDosage = findViewById(R.id.input_dosage);
        etFrequency = findViewById(R.id.input_frequency);
        etDuration = findViewById(R.id.input_duration);
        btnSave = findViewById(R.id.btn_save_medicine);

        // Charger la liste des patients
        loadPatients();

        btnSave.setOnClickListener(v -> {
            if (spinnerPatient.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Veuillez sélectionner un patient", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedIndex = spinnerPatient.getSelectedItemPosition() - 1; // -1 car le premier élément est "Sélectionner..."
            String email = patientEmails.get(selectedIndex);
            String medicine = etMedicineName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String frequency = etFrequency.getText().toString().trim();
            String duration = etDuration.getText().toString().trim();

            if (medicine.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir le nom du médicament", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.addPatientMedicine(email, medicine, dosage, frequency, duration)) {
                Toast.makeText(this, "Médicament ajouté avec succès", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatients() {
        patientNames.clear();
        patientEmails.clear();
        patientNames.add("Sélectionner un patient...");
        
        Cursor c = db.getAllUsers();
        if (c != null) {
            while (c.moveToNext()) {
                String role = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE));
                if ("Patient".equals(role)) {
                    String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                    String email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL));
                    patientNames.add(name);
                    patientEmails.add(email);
                }
            }
            c.close();
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, patientNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatient.setAdapter(adapter);
    }
}
