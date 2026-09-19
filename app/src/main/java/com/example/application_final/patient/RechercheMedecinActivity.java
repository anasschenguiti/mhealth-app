package com.example.application_final.patient;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RechercheMedecinActivity extends AppCompatActivity {

    EditText inputSearch;
    RecyclerView recyclerDoctors;
    DoctorAdapter adapter;
    DatabaseHelper db;
    List<Doctor> doctorList = new ArrayList<>();
    String userEmail;
    String patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recherche_medecin);

        db = DatabaseHelper.getInstance(this);
        inputSearch = findViewById(R.id.inputSearch);
        recyclerDoctors = findViewById(R.id.recyclerDoctors);

        // Get Patient Info
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        // Try to get name directly or fetch it
        if (userEmail == null) {
            // Fallback or try to get from previous activity if passed, or just "Patient"
             // In a real app we might store session in SharedPreferences
             // Here we assume it's passed or we fetch if we can. 
             // Ideally MainActivityPatient passes it.
             // If null, we might face issues booking.
        } else {
            patientName = db.getUserName(userEmail);
        }

        adapter = new DoctorAdapter(doctorList, this::showBookingDialog);
        recyclerDoctors.setLayoutManager(new LinearLayoutManager(this));
        recyclerDoctors.setAdapter(adapter);

        loadDoctors("");

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadDoctors(s.toString());
            }
        });
    }

    private void loadDoctors(String query) {
        Cursor c = db.searchDoctors(query);

        doctorList.clear();
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                String spec = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_SPECIALTY));
                doctorList.add(new Doctor(id, name, spec));
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showBookingDialog(Doctor doctor) {
        if (patientName == null || patientName.isEmpty()) {
            Toast.makeText(this, "Erreur: Impossible d'identifier le patient", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute);
                confirmBooking(doctor, date, time);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
            
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void confirmBooking(Doctor doctor, String date, String time) {
        if (db.addAppointment(patientName, date, time, doctor.id)) {
            Toast.makeText(this, "Rendez-vous pris avec " + doctor.name + " le " + date + " à " + time, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Erreur lors de la prise de rendez-vous", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Inner Models & Adapter ---

    static class Doctor {
        int id;
        String name, specialty;

        public Doctor(int id, String name, String specialty) {
            this.id = id;
            this.name = name;
            this.specialty = specialty;
        }
    }

    interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    static class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {
        List<Doctor> list;
        OnDoctorClickListener listener;

        public DoctorAdapter(List<Doctor> list, OnDoctorClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medecin, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Doctor d = list.get(position);
            holder.name.setText(d.name);
            holder.spec.setText(d.specialty);
            holder.itemView.setOnClickListener(v -> listener.onDoctorClick(d));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, spec;

            public ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.docName);
                spec = v.findViewById(R.id.docSpec);
            }
        }
    }
}
