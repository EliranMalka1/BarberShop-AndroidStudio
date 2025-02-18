package com.example.navigtion_app.models;

public class User {
     String email;
     String phone;

     String fullName;

    public User(String email, String phone,String fullName) {
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;

    }

    public User()
    {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getfullName() {
        return fullName;
    }

    public void setfullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
