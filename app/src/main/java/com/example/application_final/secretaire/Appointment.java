package com.example.application_final.secretaire;

public class Appointment {
    private int id;
    private int doctorId;
    private String patientName;
    private String date;
    private String time;
    private String status;

    public Appointment(int id, int doctorId, String patientName, String date, String time, String status) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
