package com.example.application_final.secretaire;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class rendezvous extends AppCompatActivity {

    RecyclerView recyclerView;
    RendezvousAdapter adapter;
    DatabaseHelper db;
    List<Appointment> list = new ArrayList<>();
    TextView noData;
    Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rendezvous);

        db = DatabaseHelper.getInstance(this);
        recyclerView = findViewById(R.id.recyclerViewRdv);
        noData = findViewById(R.id.no_data_rdv);
        btnAdd = findViewById(R.id.addButton);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(rendezvous.this, AjouterRendezvousActivity.class)));

        adapter = new RendezvousAdapter(this, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        list.clear();
        Cursor c = db.getAllAppointments();
        if (c != null) {
            if (c.getCount() == 0) {
                noData.setVisibility(View.VISIBLE);
            } else {
                noData.setVisibility(View.GONE);
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_ID));
                    int docId = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_DOC_ID));
                    String patient = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_PATIENT_NAME));
                    String date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_DATE));
                    String time = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_TIME));
                    String status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_STATUS));

                    list.add(new Appointment(id, docId, patient, date, time, status));
                }
            }
            c.close();
        }
        adapter.notifyDataSetChanged();
    }
}
