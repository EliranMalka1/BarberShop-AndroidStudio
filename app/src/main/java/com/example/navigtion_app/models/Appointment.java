package com.example.navigtion_app.models;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        this.date = cleanDate(date);
        this.time = cleanTime(time);
    }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getBarberId() { return barberId; }
    public void setBarberId(String barberId) { this.barberId = barberId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = cleanDate(date); }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = cleanTime(time); }

    /**
     * מסיר רווחים מיותרים ושורות חדשות כדי לוודא שהתאריך בפורמט תקין
     */
    private String cleanDate(String date) {
        if (date == null) return "";
        return date.replaceAll("\n", " ").trim(); // מסיר שורות חדשות ומרווחים
    }

    private String cleanTime(String time) {
        if (time == null) return "";
        return time.trim(); // מסיר רווחים מיותרים
    }

    /**
     * פונקציה שמחזירה את התאריך והשעה בפורמט אחיד לצורך השוואה
     */
    public Date getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy HH:mm", Locale.getDefault());
        String fullDate = date + " " + time;

        try {
            Date parsedDate = sdf.parse(fullDate);
            Log.d("Appointment", "Parsed date successfully: " + fullDate);
            return parsedDate;
        } catch (ParseException e) {
            Log.e("Appointment", "Failed to parse date: " + fullDate, e);
            return null;
        }
    }
}
