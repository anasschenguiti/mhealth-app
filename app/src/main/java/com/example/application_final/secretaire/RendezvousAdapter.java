package com.example.application_final.secretaire;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.util.List;

public class RendezvousAdapter extends RecyclerView.Adapter<RendezvousAdapter.ViewHolder> {

    Context context;
    List<Appointment> list;
    DatabaseHelper db;

    public RendezvousAdapter(Context context, List<Appointment> list) {
        this.context = context;
        this.list = list;
        db = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_rendezvous, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment apt = list.get(position);
        holder.patient.setText(apt.getPatientName());
        holder.time.setText(apt.getTime() + " (" + apt.getDate() + ")");

        // Fetch Doctor Name
        Cursor c = db.getUserById(String.valueOf(apt.getDoctorId()));
        if (c != null && c.moveToFirst()) {
            String docName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
            holder.doctor.setText("Dr. " + docName);
            c.close();
        } else {
            holder.doctor.setText("Dr. Inconnu");
        }

        holder.btnDelete.setOnClickListener(v -> {
            db.deleteAppointment(String.valueOf(apt.getId()));
            list.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());
            Toast.makeText(context, "Rendez-vous annulé", Toast.LENGTH_SHORT).show();
        });

        // Modify Appointment functionality
        holder.btnEdit.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, ModifierRendezvousActivity.class);
            intent.putExtra("APT_ID", apt.getId());
            intent.putExtra("APT_PATIENT", apt.getPatientName());
            intent.putExtra("APT_DATE", apt.getDate());
            intent.putExtra("APT_TIME", apt.getTime());
            intent.putExtra("APT_DOC_ID", apt.getDoctorId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView patient, time, doctor;
        Button btnDelete, btnEdit;

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
