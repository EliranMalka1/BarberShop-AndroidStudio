package com.example.navigtion_app.models;

public class EmailRequest {
    private String barberEmail;
    private String clientEmail;
    private String date;


    public EmailRequest(String barberEmail, String clientEmail, String date) {
        this.barberEmail = barberEmail;
        this.clientEmail = clientEmail;
        this.date = date;

    }
}
