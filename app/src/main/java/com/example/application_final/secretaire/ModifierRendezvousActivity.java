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
import java.util.Calendar;
import java.util.Locale;

public class ModifierRendezvousActivity extends AppCompatActivity {

    EditText inputPatient, inputDoctor, inputDate;
    Spinner spinnerTime;
    Button btnSave;
    DatabaseHelper db;
    int appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_rendezvous);

        db = DatabaseHelper.getInstance(this);

        inputPatient = findViewById(R.id.editPatientNameMod);
        inputDate = findViewById(R.id.editRendezvousDateMod);
        spinnerTime = findViewById(R.id.spinnerRendezvousTimeMod);
        inputDoctor = findViewById(R.id.editDoctorNameMod);
        btnSave = findViewById(R.id.btnSaveRendezvousMod);

        // Populate Spinner
        String[] times = new String[] { "08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(adapter);

        // Get data from Intent
        appointmentId = getIntent().getIntExtra("APT_ID", -1);
        String pName = getIntent().getStringExtra("APT_PATIENT");
        String pDate = getIntent().getStringExtra("APT_DATE");
        String pTime = getIntent().getStringExtra("APT_TIME");
        int pDocId = getIntent().getIntExtra("APT_DOC_ID", -1);

        inputPatient.setText(pName);
        inputDate.setText(pDate);

        // Pre-select time in Spinner
        if (pTime != null) {
            for (int i = 0; i < times.length; i++) {
                if (times[i].equals(pTime)) {
                    spinnerTime.setSelection(i);
                    break;
                }
            }
        }

        // Initialize Callendar for picker
        Calendar calendar = Calendar.getInstance();

        // Try to parse existing date to set calendar
        if (pDate != null && !pDate.isEmpty()) {
            try {
                String[] parts = pDate.split("/");
                if (parts.length == 3) {
                    calendar.set(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[0]));
                }
            } catch (Exception e) {
            }
        }

        // Get Doctor Name
        Cursor c = db.getUserById(String.valueOf(pDocId));
        if (c != null && c.moveToFirst()) {
            inputDoctor.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)));
            c.close();
        }

        inputDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel(calendar);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> {
            String patient = inputPatient.getText().toString();
            String date = inputDate.getText().toString();
            String time = spinnerTime.getSelectedItem().toString();
            String doctorName = inputDoctor.getText().toString();

            if (patient.isEmpty() || date.isEmpty() || doctorName.isEmpty()) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            int docId = findDoctorId(doctorName);
            if (docId == -1) {
                Toast.makeText(this, "Médecin introuvable", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.updateAppointment(appointmentId, patient, date, time, docId)) {
                Toast.makeText(this, "Rendez-vous mis à jour", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDateLabel(Calendar cal) {
        String myFormat = "dd/MM/yyyy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(myFormat, Locale.getDefault());
        inputDate.setText(sdf.format(cal.getTime()));
    }

    private int findDoctorId(String name) {
        Cursor c = db.searchUsers(name);
        if (c != null) {
            while (c.moveToNext()) {
                String role = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE));
                if ("Médecin".equalsIgnoreCase(role)) {
                    int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                    c.close();
                    return id;
                }
            }
            c.close();
        }
        return -1;
    }
}
