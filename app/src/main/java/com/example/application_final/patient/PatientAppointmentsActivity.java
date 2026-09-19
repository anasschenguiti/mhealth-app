package com.example.application_final.patient;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import com.example.application_final.secretaire.Appointment;
import java.util.ArrayList;
import java.util.List;

public class PatientAppointmentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PatientAppointmentsAdapter adapter;
    DatabaseHelper db;
    List<Appointment> list = new ArrayList<>();
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_appointments);

        db = DatabaseHelper.getInstance(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        recyclerView = findViewById(R.id.recycler_appointments);

        adapter = new PatientAppointmentsAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadAppointments();
    }

    private void loadAppointments() {
        if (userEmail == null) return;
        String name = db.getUserName(userEmail);
        
        list.clear();
        Cursor c = db.getAppointmentsForPatient(name);
        if (c != null) {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_ID));
                int docId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_DOC_ID));
                String pName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_PATIENT_NAME));
                String date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_DATE));
                String time = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_TIME));
                String status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_STATUS));
                
                list.add(new Appointment(id, docId, pName, date, time, status));
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
        
        if (list.isEmpty()) {
            findViewById(R.id.tvNoData).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tvNoData).setVisibility(View.GONE);
        }
    }

    class PatientAppointmentsAdapter extends RecyclerView.Adapter<PatientAppointmentsAdapter.ViewHolder> {
        List<Appointment> list;
        public PatientAppointmentsAdapter(List<Appointment> list) { this.list = list; }
        
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_rendezvous, parent, false);
            return new ViewHolder(v);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Appointment a = list.get(position);
            
            Cursor c = db.getUserById(String.valueOf(a.getDoctorId()));
            if (c != null && c.moveToFirst()) {
                 String docName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                 holder.patient.setText("Dr. " + docName);
                 c.close();
            } else {
                holder.patient.setText("Dr. Inconnu");
            }

            holder.time.setText(a.getDate() + " à " + a.getTime());
            holder.doctor.setText("Status: " + a.getStatus());
            
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView patient, time, doctor;
            View btnDelete, btnEdit;
            public ViewHolder(View v) {
                super(v);
                patient = v.findViewById(R.id.rdv_patient_txt);
                time = v.findViewById(R.id.rdv_heure_txt);
                doctor = v.findViewById(R.id.rdv_medecin_txt);
                btnDelete = v.findViewById(R.id.btnSupprimerRdv);
                btnEdit = v.findViewById(R.id.btnModifierRdv);
            }
        }
    }
}
