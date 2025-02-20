package com.example.navigtion_app.models;

public class Appointment {
    private String appointmentId;
    private String clientId;
    private String barberId;
    private String date;
    private String time;

    public Appointment() {}

    public Appointment(String appointmentId, String clientId, String barberId, String date, String time) {
        this.appointmentId = appointmentId;
        this.clientId = clientId;
        this.barberId = barberId;
        this.date = date;
        this.time = time;
    }

    // גטרים וסטרים
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getBarberId() { return barberId; }
    public void setBarberId(String barberId) { this.barberId = barberId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
