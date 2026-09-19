package com.example.application_final.secretaire;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class dossier_patient extends AppCompatActivity {

    RecyclerView recyclerView;
    PatientAdapter adapter;
    DatabaseHelper db;
    List<Patient> patientList = new ArrayList<>();
    TextView noData;
    View cardAddPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dossier_patient);

        db = DatabaseHelper.getInstance(this);
        recyclerView = findViewById(R.id.recyclerView);
        noData = findViewById(R.id.no_data);
        cardAddPatient = findViewById(R.id.cardAddPatient);

        // Check if user has right to add (Secretary only)
        boolean canAdd = getIntent().getBooleanExtra("CAN_ADD", false);
        if (canAdd) {
            cardAddPatient.setVisibility(View.VISIBLE);
            cardAddPatient.setOnClickListener(v -> startActivity(new Intent(dossier_patient.this, AjouterPatientActivity.class)));
        } else {
            cardAddPatient.setVisibility(View.GONE);
        }

        adapter = new PatientAdapter(this, patientList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadPatients();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatients();
    }

    private void loadPatients() {
        patientList.clear();
        Cursor c = db.getAllPatients();
        if (c != null) {
            if (c.getCount() == 0) {
                noData.setVisibility(View.VISIBLE);
            } else {
                noData.setVisibility(View.GONE);
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_FULLNAME));
                    // Age removed
                    String antecedents = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_ANTECEDENTS));
                    String description = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PATIENT_DESCRIPTION));

                    patientList.add(new Patient(id, name, antecedents, description));
                }
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
    }
}
