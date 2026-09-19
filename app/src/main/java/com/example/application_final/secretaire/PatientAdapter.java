package com.example.application_final.secretaire;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.MyViewHolder> {

    private Context context;
    private List<Patient> patientList;
    DatabaseHelper db;

    public PatientAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
        db = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_patient, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Patient p = patientList.get(position);
        holder.patient_name_txt.setText(p.getFullname());
        // Age removed
        holder.patient_age_txt.setText(""); 
        holder.patient_antecedents_txt.setText(p.getAntecedents());

        holder.btnViewPatient.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Détails Patient")
                    .setMessage("Nom: " + p.getFullname() + "\nAntécédents: "
                            + p.getAntecedents() + "\nDescription: " + p.getDescription())
                    .setPositiveButton("Fermer", null)
                    .show();
        });

        holder.btnModifierRow.setOnClickListener(v -> {
            Intent intent = new Intent(context, ModifierPatientActivity.class);
            intent.putExtra("id", String.valueOf(p.getId()));
            context.startActivity(intent);
        });

        holder.btnSupprimerRow.setOnClickListener(v -> {
            db.deletePatient(String.valueOf(p.getId()));
            patientList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, patientList.size());
            Toast.makeText(context, "Patient supprimé", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView patient_name_txt, patient_age_txt, patient_antecedents_txt;
        Button btnModifierRow, btnSupprimerRow;
        ImageView btnViewPatient;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            patient_name_txt = itemView.findViewById(R.id.patient_name_txt);
            patient_age_txt = itemView.findViewById(R.id.patient_age_txt);
            patient_antecedents_txt = itemView.findViewById(R.id.patient_antecedents_txt);
            btnModifierRow = itemView.findViewById(R.id.btnModifierRow);
            btnSupprimerRow = itemView.findViewById(R.id.btnSupprimerRow);
            btnViewPatient = itemView.findViewById(R.id.btnViewPatient);
        }
    }
}
