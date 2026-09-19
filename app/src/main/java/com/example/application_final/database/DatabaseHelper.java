package com.example.application_final.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mhealth_unified.db";
    private static final int DATABASE_VERSION = 5; // Force DB recreation

    // --- Table Users ---
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_ROLE = "role";
    public static final String COL_USER_PHONE = "phone";
    public static final String COL_USER_SPECIALTY = "specialty";
    public static final String COL_USER_AGE = "age";
    public static final String COL_USER_SEX = "sex";
    public static final String COL_USER_ADDRESS = "address";
    public static final String COL_USER_DOB = "dob";
    public static final String COL_USER_PHOTO = "photo_uri";

    // --- Table Messages ---
    public static final String TABLE_MESSAGES = "messages";
    public static final String COL_MSG_ID = "id";
    public static final String COL_MSG_SENDER = "sender";
    public static final String COL_MSG_RECEIVER = "receiver";
    public static final String COL_MSG_CONTENT = "content";
    public static final String COL_MSG_TIME = "time";

    // --- Table Lab Results ---
    public static final String TABLE_LAB = "lab_results";
    public static final String COL_LAB_ID = "id";
    public static final String COL_LAB_PATIENT = "patient_name";
    public static final String COL_LAB_TEST = "test_type";
    public static final String COL_LAB_RESULT = "result";
    public static final String COL_LAB_DATE = "date";

    // --- Table Patients (Medical Records) ---
    public static final String TABLE_PATIENTS = "patients";
    public static final String COL_PATIENT_ID = "id";
    public static final String COL_PATIENT_USER_ID = "user_id";
    public static final String COL_PATIENT_FULLNAME = "fullname";
    public static final String COL_PATIENT_ANTECEDENTS = "antecedents";
    public static final String COL_PATIENT_DESCRIPTION = "description";

    // --- Table Appointments ---
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String COL_APT_ID = "id";
    public static final String COL_APT_DOC_ID = "doctor_id";
    public static final String COL_APT_PATIENT_NAME = "patient_name";
    public static final String COL_APT_DATE = "date";
    public static final String COL_APT_TIME = "time";
    public static final String COL_APT_STATUS = "status";

    // --- Table Medicines ---
    public static final String TABLE_MEDICINES = "medicines";
    public static final String COL_MED_ID = "id";
    public static final String COL_MED_NAME = "name";
    public static final String COL_MED_DESC = "description";

    // --- Table Patient Medicines ---
    public static final String TABLE_PATIENT_MEDICINES = "patient_medicines";
    public static final String COL_PM_ID = "id";
    public static final String COL_PM_PATIENT_EMAIL = "patient_email";
    public static final String COL_PM_MEDICINE = "medicine_name";
    public static final String COL_PM_DOSAGE = "dosage";
    public static final String COL_PM_FREQUENCY = "frequency";
    public static final String COL_PM_DURATION = "duration";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_EMAIL + " TEXT UNIQUE, " + COL_USER_PASSWORD + " TEXT, " + COL_USER_NAME + " TEXT, "
                + COL_USER_ROLE + " TEXT, " + COL_USER_PHONE + " TEXT, " + COL_USER_SPECIALTY + " TEXT, " + COL_USER_AGE
                + " INTEGER, " + COL_USER_SEX + " TEXT, " + COL_USER_ADDRESS + " TEXT, " + COL_USER_DOB + " TEXT, "
                + COL_USER_PHOTO + " TEXT)";
        db.execSQL(createUsers);

        String createPatients = "CREATE TABLE " + TABLE_PATIENTS + " (" + COL_PATIENT_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_PATIENT_USER_ID + " INTEGER, " + COL_PATIENT_FULLNAME
                + " TEXT, " + COL_PATIENT_ANTECEDENTS + " TEXT, " + COL_PATIENT_DESCRIPTION + " TEXT)";
        db.execSQL(createPatients);

        String createAppointments = "CREATE TABLE " + TABLE_APPOINTMENTS + " (" + COL_APT_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_APT_DOC_ID + " INTEGER, " + COL_APT_PATIENT_NAME
                + " TEXT, " + COL_APT_DATE + " TEXT, " + COL_APT_TIME + " TEXT, " + COL_APT_STATUS + " TEXT)";
        db.execSQL(createAppointments);

        String createMedicines = "CREATE TABLE " + TABLE_MEDICINES + " (" + COL_MED_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_MED_NAME + " TEXT, " + COL_MED_DESC + " TEXT)";
        db.execSQL(createMedicines);

        String createMessages = "CREATE TABLE " + TABLE_MESSAGES + " (" + COL_MSG_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_MSG_SENDER + " TEXT, " + COL_MSG_RECEIVER + " TEXT, "
                + COL_MSG_CONTENT + " TEXT, " + COL_MSG_TIME + " TEXT)";
        db.execSQL(createMessages);

        String createLabResults = "CREATE TABLE " + TABLE_LAB + " (" + COL_LAB_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_LAB_PATIENT + " TEXT, " + COL_LAB_TEST + " TEXT, "
                + COL_LAB_RESULT + " TEXT, " + COL_LAB_DATE + " TEXT)";
        db.execSQL(createLabResults);

        String createPatientMedicines = "CREATE TABLE " + TABLE_PATIENT_MEDICINES + " (" + COL_PM_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_PM_PATIENT_EMAIL + " TEXT, "
                + COL_PM_MEDICINE + " TEXT, " + COL_PM_DOSAGE + " TEXT, " + COL_PM_FREQUENCY + " TEXT, "
                + COL_PM_DURATION + " TEXT)";
        db.execSQL(createPatientMedicines);

        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAB);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENT_MEDICINES);
        onCreate(db);
    }

    private void seedData(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        // Admin
        cv.put(COL_USER_EMAIL, "admin");
        cv.put(COL_USER_PASSWORD, "admin");
        cv.put(COL_USER_NAME, "Super Admin");
        cv.put(COL_USER_ROLE, "Admin");
        db.insert(TABLE_USERS, null, cv);
        // Doctor
        cv.clear();
        cv.put(COL_USER_EMAIL, "medecin1");
        cv.put(COL_USER_PASSWORD, "med123");
        cv.put(COL_USER_NAME, "Dr. House");
        cv.put(COL_USER_ROLE, "Médecin");
        cv.put(COL_USER_SPECIALTY, "Diagnostic");
        db.insert(TABLE_USERS, null, cv);
        // Secretary
        cv.clear();
        cv.put(COL_USER_EMAIL, "secretaire");
        cv.put(COL_USER_PASSWORD, "secretaire123");
        cv.put(COL_USER_NAME, "Mme. Secrétaire");
        cv.put(COL_USER_ROLE, "Secrétaire");
        db.insert(TABLE_USERS, null, cv);
        // Patient
        cv.clear();
        cv.put(COL_USER_EMAIL, "patient");
        cv.put(COL_USER_PASSWORD, "patient123");
        cv.put(COL_USER_NAME, "John Doe");
        cv.put(COL_USER_ROLE, "Patient");
        db.insert(TABLE_USERS, null, cv);
        cv.clear();
        cv.put(COL_USER_EMAIL, "patient2");
        cv.put(COL_USER_PASSWORD, "patient123");
        cv.put(COL_USER_NAME, "Jane Smith");
        cv.put(COL_USER_ROLE, "Patient");
        db.insert(TABLE_USERS, null, cv);
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[] { email, password });
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public long addUser(String name, String email, String phone, String role, String sex, String dob, String address,
            String password, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_NAME, name);
        cv.put(COL_USER_EMAIL, email);
        cv.put(COL_USER_PHONE, phone);
        cv.put(COL_USER_ROLE, role);
        cv.put(COL_USER_SEX, sex);
        cv.put(COL_USER_DOB, dob);
        cv.put(COL_USER_ADDRESS, address);
        cv.put(COL_USER_PHOTO, photoUri);
        cv.put(COL_USER_PASSWORD, password);
        return db.insert(TABLE_USERS, null, cv);
    }

    public int updateUser(String id, String name, String email, String phone, String role, String sex, String dob,
            String address, String password, String photoUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_NAME, name);
        cv.put(COL_USER_EMAIL, email);
        cv.put(COL_USER_PHONE, phone);
        cv.put(COL_USER_ROLE, role);
        cv.put(COL_USER_SEX, sex);
        cv.put(COL_USER_DOB, dob);
        cv.put(COL_USER_ADDRESS, address);
        if (password != null && !password.isEmpty())
            cv.put(COL_USER_PASSWORD, password);
        cv.put(COL_USER_PHOTO, photoUri);
        return db.update(TABLE_USERS, cv, COL_USER_ID + "=?", new String[] { id });
    }

    public void deleteUser(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, COL_USER_ID + "=?", new String[] { id });
    }

    public Cursor getAllUsers() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_USERS, null);
    }

    public Cursor getUserById(String id) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ID + "=?",
                new String[] { id });
    }

    public Cursor searchUsers(String query) {
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_NAME + " LIKE ?",
                new String[] { "%" + query + "%" });
    }

    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_USER_ROLE + " FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=?",
                new String[] { email });
        if (cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            return role;
        }
        cursor.close();
        return "";
    }

    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_USER_NAME + " FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=?",
                new String[] { email });
        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }
        cursor.close();
        return "";
    }

    public String getUserPhoto(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_USER_PHOTO + " FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + "=?",
                new String[] { email });
        if (cursor.moveToFirst()) {
            String photo = cursor.getString(0);
            cursor.close();
            return photo != null ? photo : "";
        }
        cursor.close();
        return "";
    }

    public boolean addPatient(int userId, String fullname, String antecedents, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PATIENT_USER_ID, userId);
        cv.put(COL_PATIENT_FULLNAME, fullname);
        cv.put(COL_PATIENT_ANTECEDENTS, antecedents);
        cv.put(COL_PATIENT_DESCRIPTION, description);
        return db.insert(TABLE_PATIENTS, null, cv) != -1;
    }

    public boolean updatePatient(String id, String fullname, String antecedents, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PATIENT_FULLNAME, fullname);
        cv.put(COL_PATIENT_ANTECEDENTS, antecedents);
        cv.put(COL_PATIENT_DESCRIPTION, description);
        return db.update(TABLE_PATIENTS, cv, COL_PATIENT_ID + "=?", new String[] { id }) != -1;
    }

    public void deletePatient(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PATIENTS, COL_PATIENT_ID + "=?", new String[] { id });
    }

    public Cursor getAllPatients() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_PATIENTS, null);
    }

    public Cursor getPatientById(String id) {
        return this.getReadableDatabase()
                .rawQuery("SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_PATIENT_ID + "=?", new String[] { id });
    }

    public Cursor getPatientByUserId(int userId) {
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_PATIENT_USER_ID + "=?",
                new String[] { String.valueOf(userId) });
    }

    public Cursor getPatientsWithoutMedicalRecord() {
        return this.getReadableDatabase()
                .rawQuery("SELECT " + COL_USER_ID + ", " + COL_USER_NAME + " FROM " + TABLE_USERS + " WHERE "
                        + COL_USER_ROLE + "='Patient' AND " + COL_USER_ID + " NOT IN (SELECT " + COL_PATIENT_USER_ID
                        + " FROM " + TABLE_PATIENTS + ")", null);
    }

    public boolean addAppointment(String patientName, String date, String time, int doctorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_APT_PATIENT_NAME, patientName);
        cv.put(COL_APT_DATE, date);
        cv.put(COL_APT_TIME, time);
        cv.put(COL_APT_DOC_ID, doctorId);
        cv.put(COL_APT_STATUS, "Planifié");
        return db.insert(TABLE_APPOINTMENTS, null, cv) != -1;
    }

    public boolean updateAppointment(int id, String patientName, String date, String time, int doctorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_APT_PATIENT_NAME, patientName);
        cv.put(COL_APT_DATE, date);
        cv.put(COL_APT_TIME, time);
        cv.put(COL_APT_DOC_ID, doctorId);
        return db.update(TABLE_APPOINTMENTS, cv, COL_APT_ID + "=?", new String[] { String.valueOf(id) }) != -1;
    }

    public void deleteAppointment(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENTS, COL_APT_ID + "=?", new String[] { id });
    }

    public Cursor getAllAppointments() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_APPOINTMENTS, null);
    }

    public Cursor searchDoctors(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_USER_ROLE + " = ? AND " + COL_USER_NAME + " LIKE ?";
        String[] selectionArgs = { "Médecin", "%" + query + "%" };
        return db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
    }

    public Cursor getAllMedicines() {
        return this.getReadableDatabase().query(TABLE_MEDICINES, null, null, null, null, null, null);
    }

    public Cursor getAppointmentsForDoctor(int doctorId) {
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_APPOINTMENTS + " WHERE " + COL_APT_DOC_ID + "=?",
                new String[] { String.valueOf(doctorId) });
    }

    public Cursor getAppointmentsForPatient(String patientName) {
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_APPOINTMENTS + " WHERE " + COL_APT_PATIENT_NAME + "=?",
                new String[] { patientName });
    }

    public void addMessage(String sender, String receiver, String content, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MSG_SENDER, sender);
        cv.put(COL_MSG_RECEIVER, receiver);
        cv.put(COL_MSG_CONTENT, content);
        cv.put(COL_MSG_TIME, time);
        db.insert(TABLE_MESSAGES, null, cv);
    }

    public Cursor getAllMessages() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_MESSAGES, null);
    }

    public void addLabResult(String patient, String test, String result, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LAB_PATIENT, patient);
        cv.put(COL_LAB_TEST, test);
        cv.put(COL_LAB_RESULT, result);
        cv.put(COL_LAB_DATE, date);
        db.insert(TABLE_LAB, null, cv);
    }

    public Cursor getLabResults() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_LAB, null);
    }

    public Cursor getLabResultsForPatient(String patientName) {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_LAB + " WHERE " + COL_LAB_PATIENT + "=?",
                new String[] { patientName });
    }

    public int getUserIdByEmail(String email) {
        Cursor c = this.getReadableDatabase().query(TABLE_USERS, new String[] { COL_USER_ID }, COL_USER_EMAIL + "=?",
                new String[] { email }, null, null, null);
        if (c != null && c.moveToFirst()) {
            int id = c.getInt(0);
            c.close();
            return id;
        }
        return -1;
    }

    public Cursor getMessagesBetween(String user1, String user2) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_MESSAGES +
                " WHERE (" + COL_MSG_SENDER + "=? AND " + COL_MSG_RECEIVER + "=?) OR (" +
                COL_MSG_SENDER + "=? AND " + COL_MSG_RECEIVER + "=?) ORDER BY " + COL_MSG_ID + " ASC",
                new String[] { user1, user2, user2, user1 });
    }

    public Cursor getPotentialContacts(String currentUserRole) {
        SQLiteDatabase db = this.getReadableDatabase();
        if ("Patient".equals(currentUserRole)) {
            // Patients can chat with Doctors
            return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ROLE + "='Médecin' OR "
                    + COL_USER_ROLE + "='Secrétaire'", null);
        } else {
            // Doctors/Secretaries can chat with Patients
            return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ROLE + "='Patient'", null);
        }
    }

    public boolean addPatientMedicine(String patientEmail, String medicineName, String dosage, String frequency,
            String duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PM_PATIENT_EMAIL, patientEmail);
        cv.put(COL_PM_MEDICINE, medicineName);
        cv.put(COL_PM_DOSAGE, dosage);
        cv.put(COL_PM_FREQUENCY, frequency);
        cv.put(COL_PM_DURATION, duration);
        return db.insert(TABLE_PATIENT_MEDICINES, null, cv) != -1;
    }

    public Cursor getMedicinesForPatient(String patientEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PATIENT_MEDICINES + " WHERE " + COL_PM_PATIENT_EMAIL + "=?",
                new String[] { patientEmail });
    }
}
