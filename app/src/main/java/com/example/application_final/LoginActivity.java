package com.example.application_final;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application_final.admin.MainActivity;
import com.example.application_final.database.DatabaseHelper;
import com.example.application_final.medcin.MainActivityDoctor; // Renaming to avoid confusion
import com.example.application_final.patient.MainActivityPatient;
import com.example.application_final.secretaire.MainActivitySecretary; // Renaming for clarity

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginButton;
    TextView errorText;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = DatabaseHelper.getInstance(this);

        emailInput = findViewById(R.id.edit_email); // Ensure IDs match layout
        passwordInput = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_login);
        errorText = findViewById(R.id.text_error);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String pass = passwordInput.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Veuillez remplir les champs", Toast.LENGTH_SHORT).show();
                } else {
                    if (db.checkUser(email, pass)) {
                        String role = db.getUserRole(email);
                        navigateUser(role, email);
                    } else {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Identifiants incorrects");
                    }
                }
            }
        });
    }

    private void navigateUser(String role, String email) {
        Intent intent = null;
        switch (role) {
            case "Admin":
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
            case "Médecin":
                intent = new Intent(LoginActivity.this, MainActivityDoctor.class);
                break;
            case "Secrétaire":
                intent = new Intent(LoginActivity.this, MainActivitySecretary.class);
                break;
            case "Patient":
                intent = new Intent(LoginActivity.this, MainActivityPatient.class);
                break;
            default:
                Toast.makeText(this, "Rôle inconnu: " + role, Toast.LENGTH_SHORT).show();
                return;
        }
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }
}
