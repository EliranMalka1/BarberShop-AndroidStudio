package com.example.navigtion_app.models;

import java.util.UUID;

public class User {
    private String id;
    private String email;
    private String phone;
    private String fullName;

    private String type;

    // Default constructor required for Firebase deserialization
    public User() {
    }

    public User(String email, String phone, String fullName) {
        // Generate a unique ID using UUID
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.type = "client";
    }

    public User(String email,String phone, String fullName,String type)
    {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.type = type;
    }

    // Getter and setter for id
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // Getter and setter for email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter for phone
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Getter and setter for fullName
    public String getFullName() {
        return this.fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
