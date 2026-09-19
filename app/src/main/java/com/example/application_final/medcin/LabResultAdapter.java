package com.example.application_final.medcin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import java.util.List;

public class LabResultAdapter extends RecyclerView.Adapter<LabResultAdapter.ViewHolder> {

    private List<LabResult> list;

    public LabResultAdapter(List<LabResult> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lab_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LabResult l = list.get(position);
        holder.patient.setText(l.getPatient());
        holder.test.setText(l.getTest() + " - " + l.getResult());
        holder.date.setText(l.getDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView patient, test, date;

        public ViewHolder(View v) {
            super(v);
            patient = v.findViewById(R.id.lab_patient);
            test = v.findViewById(R.id.lab_test);
            date = v.findViewById(R.id.lab_date);
        }
    }
}
