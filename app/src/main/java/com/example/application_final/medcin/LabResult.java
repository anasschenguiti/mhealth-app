package com.example.application_final.medcin;

public class LabResult {
    private String patient;
    private String test;
    private String result;
    private String date;

    public LabResult(String patient, String test, String result, String date) {
        this.patient = patient;
        this.test = test;
        this.result = result;
        this.date = date;
    }

    public String getPatient() {
        return patient;
    }

    public String getTest() {
        return test;
    }

    public String getResult() {
        return result;
    }

    public String getDate() {
        return date;
    }
}
