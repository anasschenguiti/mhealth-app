package com.example.application_final.medcin;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.application_final.R;
import com.example.application_final.database.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class planning extends AppCompatActivity {

    DatabaseHelper db;
    String userEmail;
    LinearLayout morningSlots, afternoonSlots;
    GridLayout calendarGrid;
    Spinner spinnerDoctor;
    String selectedDateStr; // Format dd/MM/yyyy
    List<String> doctorNames = new ArrayList<>();
    List<Integer> doctorIds = new ArrayList<>();
    int selectedDoctorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        db = DatabaseHelper.getInstance(this);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        morningSlots = findViewById(R.id.morning_slots);
        afternoonSlots = findViewById(R.id.afternoon_slots);
        calendarGrid = findViewById(R.id.calendar_grid);
        spinnerDoctor = findViewById(R.id.spinner_doctor);

        // Vérifier si c'est une secrétaire
        String role = db.getUserRole(userEmail);
        if ("Secrétaire".equals(role)) {
            // Afficher le spinner de sélection de médecin
            if (spinnerDoctor != null) {
                spinnerDoctor.setVisibility(View.VISIBLE);
                loadDoctors();
                spinnerDoctor.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0 && position <= doctorIds.size()) {
                            selectedDoctorId = doctorIds.get(position - 1);
                            refreshAppointments();
                        } else {
                            selectedDoctorId = -1;
                            // Vider les créneaux si aucun médecin sélectionné
                            morningSlots.removeAllViews();
                            afternoonSlots.removeAllViews();
                        }
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        selectedDoctorId = -1;
                    }
                });
            }
        } else {
            // Cacher le spinner pour les médecins
            if (spinnerDoctor != null) {
                spinnerDoctor.setVisibility(View.GONE);
            }
        }

        setupCalendar();
    }

    private void loadDoctors() {
        doctorNames.clear();
        doctorIds.clear();
        doctorNames.add("Sélectionner un médecin...");
        
        Cursor c = db.getAllUsers();
        if (c != null) {
            while (c.moveToNext()) {
                String role = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE));
                if ("Médecin".equals(role)) {
                    int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                    doctorNames.add(name);
                    doctorIds.add(id);
                }
            }
            c.close();
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoctor.setAdapter(adapter);
    }

    private void setupCalendar() {
        calendarGrid.removeAllViews();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE\ndd", Locale.FRENCH);
        SimpleDateFormat dbFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // If no date selected, default to today
        if (selectedDateStr == null) {
            selectedDateStr = dbFormat.format(cal.getTime());
        }

        for (int i = 0; i < 7; i++) {
            Date date = cal.getTime();
            String dayText = displayFormat.format(date);
            String dateKey = dbFormat.format(date);

            CardView card = new CardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 160;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            card.setLayoutParams(params);
            card.setRadius(20f);
            card.setCardElevation(4f);

            TextView tv = new TextView(this);
            tv.setText(dayText);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(12f);
            card.addView(tv);

            if (selectedDateStr.equals(dateKey)) {
                card.setCardBackgroundColor(Color.parseColor("#FF8A65")); // Selected
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                card.setCardBackgroundColor(Color.WHITE); // Unselected
                tv.setTextColor(Color.parseColor("#333333"));
            }

            card.setOnClickListener(v -> {
                selectedDateStr = dateKey;
                setupCalendar(); // Recursively call to refresh UI state
                refreshAppointments();
            });

            calendarGrid.addView(card);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Ensure appointments are refreshed after calendar setup
        refreshAppointments();
    }

    private void refreshAppointments() {
        if (userEmail != null) {
            String role = db.getUserRole(userEmail);
            if ("Secrétaire".equals(role)) {
                // Si un médecin est sélectionné, charger ses rendez-vous
                if (selectedDoctorId != -1) {
                    loadAppointments(selectedDoctorId);
                } else {
                    // Aucun médecin sélectionné, vider les créneaux
                    morningSlots.removeAllViews();
                    afternoonSlots.removeAllViews();
                }
            } else {
                int doctorId = db.getUserIdByEmail(userEmail);
                if (doctorId != -1) {
                    loadAppointments(doctorId);
                }
            }
        }
    }


    private void loadAppointments(int doctorId) {
        populateOccupiedTimes(db.getAppointmentsForDoctor(doctorId));
    }

    private void populateOccupiedTimes(Cursor c) {
        HashSet<String> occupiedTimes = new HashSet<>();
        if (c != null) {
            while (c.moveToNext()) {
                String date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_DATE));
                String time = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_TIME));

                // Comparaison basique des dates
                // On suppose que le format est cohérent (dd/MM/yyyy)
                if (date != null && date.equals(selectedDateStr)) {
                    occupiedTimes.add(time);
                }
            }
            c.close();
        }
        updateSlotsUI(occupiedTimes);
    }

    private void updateSlotsUI(HashSet<String> occupiedTimes) {
        morningSlots.removeAllViews();
        afternoonSlots.removeAllViews();

        String[] morningTimes = { "08:00", "09:00", "10:00", "11:00" };
        String[] afternoonTimes = { "14:00", "15:00", "16:00", "17:00" };

        populateSlots(morningSlots, morningTimes, occupiedTimes);
        populateSlots(afternoonSlots, afternoonTimes, occupiedTimes);
    }

    private void populateSlots(LinearLayout container, String[] times, HashSet<String> occupiedTimes) {
        for (String t : times) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            card.setLayoutParams(params);
            card.setRadius(20f);

            TextView tv = new TextView(this);
            tv.setPadding(32, 24, 32, 24);

            if (occupiedTimes.contains(t)) {
                card.setCardBackgroundColor(Color.parseColor("#FFCDD2")); // Red for occupied
                tv.setTextColor(Color.RED);
                tv.setText(t + " (Pris)");

                // Add click listener to show details
                card.setOnClickListener(v -> {
                    showAppointmentDetails(t);
                });

            } else {
                card.setCardBackgroundColor(Color.parseColor("#E0F7FA")); // Blue for available
                tv.setTextColor(Color.BLACK);
                tv.setText(t);
            }

            card.addView(tv);
            container.addView(card);
        }
    }

    private void showAppointmentDetails(String time) {
        // Find the appointment for this time and date
        if (userEmail == null)
            return;

        int doctorId;
        String role = db.getUserRole(userEmail);
        if ("Secrétaire".equals(role)) {
            // Utiliser le médecin sélectionné
            if (selectedDoctorId == -1) {
                android.widget.Toast.makeText(this, "Veuillez sélectionner un médecin", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            doctorId = selectedDoctorId;
        } else {
            doctorId = db.getUserIdByEmail(userEmail);
        }
        
        Cursor c = db.getAppointmentsForDoctor(doctorId);

        if (c != null) {
            boolean found = false;
            while (c.moveToNext()) {
                String aDate = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_DATE));
                String aTime = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_TIME));

                if (aDate.equals(selectedDateStr) && aTime.equals(time)) {
                    String patient = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_PATIENT_NAME));
                    String status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_APT_STATUS));

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Détails du Rendez-vous")
                            .setMessage("Patient: " + patient + "\nHeure: " + time + "\nDate: " + selectedDateStr
                                    + "\nStatut: " + status)
                            .setPositiveButton("OK", null)
                            .show();
                    found = true;
                    break;
                }
            }
            c.close();
            if (!found) {
                android.widget.Toast.makeText(this, "Rendez-vous non trouvé", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
}
