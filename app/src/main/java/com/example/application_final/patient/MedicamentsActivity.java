package com.example.application_final.patient;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class MedicamentsActivity extends AppCompatActivity {

    RecyclerView recyclerMedicaments;
    MedicamentAdapter adapter;
    DatabaseHelper db;
    List<Medicament> medicamentList = new ArrayList<>();
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicaments);

        db = DatabaseHelper.getInstance(this);

        // Try getting email from Intent, if not found try to get from SharedPreferences
        // or similar if implemented
        // Here we rely on previous activity passing it.
        // If coming from MainActivityPatient, it does not pass USER_EMAIL currently, so
        // we must assume it might be missing
        // or check if we can get it.
        // Actually, MainActivityPatient creates the intent without extras at line 47.
        // We must fix that too.
        // For now, let's try to get it.
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        recyclerMedicaments = findViewById(R.id.recyclerMedicaments);
        adapter = new MedicamentAdapter(medicamentList);
        recyclerMedicaments.setLayoutManager(new LinearLayoutManager(this));
        recyclerMedicaments.setAdapter(adapter);

        if (userEmail != null) {
            loadMedicines(userEmail);
        } else {
            // Fallback or show empty
            Toast.makeText(this, "Email introuvable", Toast.LENGTH_SHORT).show();
            // Maybe try to load all medicines as fallback? No, that's not right.
            // We will modify MainActivityPatient to pass the email.
        }
    }

    private void loadMedicines(String email) {
        Cursor c = db.getMedicinesForPatient(email);

        medicamentList.clear();
        if (c != null) {
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PM_MEDICINE));
                String dosage = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PM_DOSAGE));
                String freq = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PM_FREQUENCY));
                String dur = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PM_DURATION));
                medicamentList.add(new Medicament(name, dosage + " - " + freq + " (" + dur + ")"));
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
    }

    // --- Inner Models & Adapter ---

    static class Medicament {
        String name, description;

        public Medicament(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    static class MedicamentAdapter extends RecyclerView.Adapter<MedicamentAdapter.ViewHolder> {
        List<Medicament> list;

        public MedicamentAdapter(List<Medicament> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicament, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Medicament m = list.get(position);
            holder.name.setText(m.name);
            holder.desc.setText(m.description);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, desc;

            public ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.medName);
                desc = v.findViewById(R.id.medTime); // Using existing IDs from item_medicament.xml
            }
        }
    }
}
