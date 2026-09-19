package com.example.application_final.secretaire;

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
import java.util.ArrayList;
import java.util.List;

public class AjouterPatientActivity extends AppCompatActivity {

    Spinner spinnerPatient;
    EditText antecedentsInput, descriptionInput;
    Button saveButton;
    DatabaseHelper db;
    List<UserItem> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_patient);

        db = DatabaseHelper.getInstance(this);

        spinnerPatient = findViewById(R.id.spinnerPatientUser);
        antecedentsInput = findViewById(R.id.editPatientAntecedents);
        descriptionInput = findViewById(R.id.editPatientDescription);
        saveButton = findViewById(R.id.btnSavePatient);

        loadPatientsSpinner();

        saveButton.setOnClickListener(v -> {
            if (userList.isEmpty() || spinnerPatient.getSelectedItem() == null) {
                Toast.makeText(this, "Aucun patient sélectionné", Toast.LENGTH_SHORT).show();
                return;
            }
            
            UserItem selectedUser = (UserItem) spinnerPatient.getSelectedItem();
            String antecedents = antecedentsInput.getText().toString();
            String desc = descriptionInput.getText().toString();

            if (db.addPatient(selectedUser.id, selectedUser.name, antecedents, desc)) {
                Toast.makeText(this, "Dossier patient créé avec succès", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de la création", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientsSpinner() {
        userList = new ArrayList<>();
        Cursor cursor = db.getPatientsWithoutMedicalRecord();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                userList.add(new UserItem(id, name));
            }
            cursor.close();
        }

        ArrayAdapter<UserItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatient.setAdapter(adapter);
    }

    private static class UserItem {
        int id;
        String name;

        public UserItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
