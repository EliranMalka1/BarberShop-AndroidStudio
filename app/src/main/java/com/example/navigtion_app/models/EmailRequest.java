package com.example.navigtion_app.models;

public class EmailRequest {
    private String barberEmail;
    private String clientEmail;
    private String date;
    private String time;

    public EmailRequest(String barberEmail, String clientEmail, String date, String time) {
        this.barberEmail = barberEmail;
        this.clientEmail = clientEmail;
        this.date = date;
        this.time = time;
    }
}
