package com.example.application_final.patient;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import com.example.application_final.medcin.LabResult;
import com.example.application_final.medcin.LabResultAdapter;
import java.util.ArrayList;
import java.util.List;

public class PatientLabResultsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LabResultAdapter adapter;
    DatabaseHelper db;
    List<LabResult> list = new ArrayList<>();
    String userEmail;
    TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_lab_results);

        db = DatabaseHelper.getInstance(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        
        recyclerView = findViewById(R.id.recycler_lab_results);
        tvNoData = findViewById(R.id.tvNoData);

        adapter = new LabResultAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadResults();
    }

    private void loadResults() {
        if (userEmail == null) return;
        
        String patientName = db.getUserName(userEmail); // Fetch Name from Email
        if (patientName.isEmpty()) return;

        list.clear();
        Cursor c = db.getLabResultsForPatient(patientName);
        if (c != null) {
            while (c.moveToNext()) {
                String p = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAB_PATIENT));
                String t = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAB_TEST));
                String r = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAB_RESULT));
                String d = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_LAB_DATE));
                list.add(new LabResult(p, t, r, d));
            }
            c.close();
        }
        
        if (list.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            tvNoData.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
}
