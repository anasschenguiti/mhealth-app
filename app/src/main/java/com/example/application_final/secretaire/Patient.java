package com.example.application_final.secretaire;

public class Patient {
    private int id;
    private String fullname;
    private String antecedents;
    private String description;

    public Patient(int id, String fullname, String antecedents, String description) {
        this.id = id;
        this.fullname = fullname;
        this.antecedents = antecedents;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getAntecedents() {
        return antecedents;
    }

    public String getDescription() {
        return description;
    }
}
