package com.example.navigtion_app.models;

import java.util.UUID;

public class User {
    private String id;
    private String email;
    private String phone;
    private String fullName;
    private String type;

    private String favorite;

    public User() {
    }

    public User(String email, String phone, String fullName) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.type = "client";
        this.favorite =null;
    }

    public User(String email,String phone, String fullName,String type)
    {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.type = type;
        this.favorite =null;
    }

    public User(String id, String email, String phone, String fullName, String type) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.type = type;
        this.favorite = null;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

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

    public String getFavorite() {
        return favorite;
    }
    public void setFavorite(String favorite) {
        this.favorite = favorite;
    }
}
