package com.example.application_final.secretaire;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AjouterRendezvousActivity extends AppCompatActivity {

    Spinner spinnerPatient, spinnerDoctor;
    EditText inputDate;
    Spinner spinnerTime;
    Button btnSave;
    DatabaseHelper db;
    List<String> patientNames = new ArrayList<>();
    List<String> doctorNames = new ArrayList<>();
    List<Integer> doctorIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_rendezvous);

        db = DatabaseHelper.getInstance(this);
        spinnerPatient = findViewById(R.id.spinnerPatient);
        inputDate = findViewById(R.id.editRendezvousDate);
        spinnerTime = findViewById(R.id.spinnerRendezvousTime);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        btnSave = findViewById(R.id.btnSaveRendezvous);

        // Charger les listes de patients et médecins
        loadPatients();
        loadDoctors();

        // Populate Spinner with time slots
        String[] times = new String[] { "08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(adapter);

        // Date Picker logic
        Calendar calendar = Calendar.getInstance();
        updateDateLabel(calendar); // Set default to today

        inputDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel(calendar);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> {
            if (spinnerPatient.getSelectedItemPosition() == 0 || spinnerDoctor.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Veuillez sélectionner un patient et un médecin", Toast.LENGTH_SHORT).show();
                return;
            }

            String patient = spinnerPatient.getSelectedItem().toString();
            String date = inputDate.getText().toString();
            String time = spinnerTime.getSelectedItem().toString();
            int selectedDoctorIndex = spinnerDoctor.getSelectedItemPosition() - 1; // -1 car le premier élément est "Sélectionner..."

            if (date.isEmpty() || selectedDoctorIndex < 0 || selectedDoctorIndex >= doctorIds.size()) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            int docId = doctorIds.get(selectedDoctorIndex);

            if (db.addAppointment(patient, date, time, docId)) {
                Toast.makeText(this, "Rendez-vous ajouté", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatients() {
        patientNames.clear();
        patientNames.add("Sélectionner un patient..."); // Premier élément pour le placeholder
        
        Cursor c = db.getAllUsers();
        if (c != null) {
            while (c.moveToNext()) {
                String role = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE));
                if ("Patient".equals(role)) {
                    String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                    patientNames.add(name);
                }
            }
            c.close();
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, patientNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatient.setAdapter(adapter);
    }

    private void loadDoctors() {
        doctorNames.clear();
        doctorIds.clear();
        doctorNames.add("Sélectionner un médecin..."); // Premier élément pour le placeholder
        
        Cursor c = db.getAllUsers();
        if (c != null) {
            while (c.moveToNext()) {
                String role = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE));
                if ("Médecin".equals(role)) {
                    int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                    doctorNames.add(name);
                    doctorIds.add(id);
                }
            }
            c.close();
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(adapter);
    }

    private void updateDateLabel(Calendar cal) {
        String myFormat = "dd/MM/yyyy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(myFormat, java.util.Locale.getDefault());
        inputDate.setText(sdf.format(cal.getTime()));
    }

}
